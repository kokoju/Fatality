/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import Client.Stats;
import Models.Command;
import Models.CommandFactory;
import Models.CommandType;
import Models.CommandApplyAttack;
import Models.CommandComodinStatus;
import Models.CommandSelectPlayer;
import Models.CommandUpdateStats;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.json.*;

/**
 *
 * @author diego
 */
public class Server {
    private final int PORT = 35500;
    private ServerSocket serverSocket;
    private LinkedList<ServerThread> connectedClients; // arreglo de hilos por cada cliente conectado
    // referencia a la pantalla
    private ServerFrame refFrame;
    private ConnectionThread connectionsThread;
    private File archivoEstadisticas = new File("ranking.txt");
    private LinkedHashMap<String, Stats> hashMapEstadisticas;  // Un LinkedHashMap funciona como un HashMap (llave, valor), pero con la estructura de una lista enlazada, dónde los elementos se siguen entre si
    private static final long COMODIN_INTERVAL_MS = 5 * 60 * 1000L;
    private final Object comodinLock = new Object();
    private Timer comodinTimer;
    private String jugadorActualConComodin;
    private final Random comodinRandom = new Random();
    private final Set<String> solicitudesEmpate = Collections.synchronizedSet(new HashSet<>());
    private String primerProponenteEmpate;

    // Juego iniciado?
    private boolean start = false;

    public Server(ServerFrame refFrame) {
        connectedClients = new LinkedList<ServerThread>();
        this.refFrame = refFrame;
        this.iniciarStats();  // Se establece una función para recuperar las estadísticas de cada jugador (almacenado en this.hashMapEstadisticas)
        this.init();
        this.connectionsThread = new ConnectionThread(this);
        this.connectionsThread.start();
        this.initComodinScheduler();

    }

    // método que inicializa el server
    private void init() {
        try {
            serverSocket = new ServerSocket(PORT);
            refFrame.writeMessage("Server running!!!");

        } catch (IOException ex) {
            refFrame.writeMessage("Error: " + ex.getMessage());
        }
    }

    public void startGame(ServerThread origin) {

        // Si el juego ya esta iniciado, no deja entrar a mas jugadores
        if (start) {
            String[] args = new String[] { "RESULT", "Server: Juego ya iniciado" };
            Command comando = CommandFactory.getCommand(args);
            comando.setIsBroadcast(false);
            try {
                origin.objectSender.writeObject(comando);
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            return;
        }

        // Si no esta iniciado, marcar como listo al que manda el comando
        origin.isReady = true;
        boolean isReady = true;

        // Imprimir lista de clientes listos o no para jugar
        this.refFrame.writeMessage("Clientes listos para jugar: ");

        for (ServerThread client : connectedClients) {
            if (!client.isReady) {
                isReady = false;
                this.refFrame.writeMessage(client.name + ": No esta listo");
                continue;
            }
            this.refFrame.writeMessage(client.name + ": Esta listo");
        }

        // Iniciar juego si todos estan listos y si hay 2 jugadores o mas conectados
        if (isReady && connectedClients.size() >= 2) {
            this.refFrame.writeMessage("Todos listos para jugar");
            declareActiveClients();
            this.nextTurn(); // Primer turno
            start = true;
        }
    }

    // Inicia un temporizador que reparte el comodín cada 5 minutos solo una vez por instancia.
    private void initComodinScheduler() {
        if (this.comodinTimer != null)
            return;
        this.comodinTimer = new Timer("comodin-timer", true);
        this.comodinTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                asignarComodinAleatorio();
            }
        }, COMODIN_INTERVAL_MS, COMODIN_INTERVAL_MS);
    }

    // Selecciona un jugador activo al azar para asignarle el comodín.
    private void asignarComodinAleatorio() {
        List<ServerThread> candidatos;
        synchronized (connectedClients) {
            candidatos = connectedClients.stream()
                    .filter(client -> client != null && client.isActive && client.name != null && !client.name.isEmpty())
                    .collect(Collectors.toList());
        }

        if (candidatos.isEmpty()) {
            actualizarTitularComodin(null);
            return;
        }

        ServerThread elegido = candidatos.get(comodinRandom.nextInt(candidatos.size()));
        if (elegido != null)
            actualizarTitularComodin(elegido.name);
    }

    // Actualiza el registro del jugador que posee el comodín y notifica cambios.
    private void actualizarTitularComodin(String nuevoTitular) {
        String anterior;
        synchronized (comodinLock) {
            anterior = this.jugadorActualConComodin;
            this.jugadorActualConComodin = nuevoTitular;
        }

        if (anterior != null && (nuevoTitular == null || !anterior.equalsIgnoreCase(nuevoTitular))) {
            enviarEstadoComodin(anterior, false, "Has perdido el comodín. Espera el próximo sorteo.");
        }

        if (nuevoTitular != null && (anterior == null || !anterior.equalsIgnoreCase(nuevoTitular))) {
            enviarEstadoComodin(nuevoTitular, true,
                    "¡Has recibido el comodín! Usa 'Attack <Jugador> Comodin <Peleador1> <Arma1> <Peleador2> <Arma2>'.");
            anunciarNuevoComodin(nuevoTitular);
        }
    }

    // Envía al jugador objetivo la notificación privada sobre su estado del comodín.
    private void enviarEstadoComodin(String jugador, boolean disponible, String mensaje) {
        if (jugador == null)
            return;
        Command estado = new CommandComodinStatus(jugador, disponible, mensaje);
        enviarComandoAJugador(jugador, estado);
    }

    // Anuncia por broadcast quién recibió el comodín recientemente.
    private void anunciarNuevoComodin(String jugador) {
        if (jugador == null)
            return;
        String mensaje = "Server: " + jugador + " recibió el comodín durante 5 minutos.";
        this.refFrame.writeMessage(mensaje);
        for (ServerThread client : connectedClients) {
            if (client == null)
                continue;
            try {
                Command aviso = CommandFactory.getCommand(new String[] { "RESULT", mensaje });
                client.objectSender.writeObject(aviso);
            } catch (IOException ignored) {
            }
        }
    }

    // Marca el comodín como consumido por el jugador indicado y actualiza la interfaz.
    public void registrarComodinUsadoPor(String jugador) {
        if (jugador == null)
            return;
        boolean eraTitular = false;
        synchronized (comodinLock) {
            if (this.jugadorActualConComodin != null
                    && this.jugadorActualConComodin.equalsIgnoreCase(jugador)) {
                eraTitular = true;
                this.jugadorActualConComodin = null;
            }
        }

        if (eraTitular) {
            enviarEstadoComodin(jugador, false, "Tu comodín ha sido consumido.");
            this.refFrame.writeMessage("El jugador " + jugador + " consumió su comodín.");
        }
    }

    public boolean yaPropusoEmpate(String jugador) {
        if (jugador == null)
            return false;
        synchronized (solicitudesEmpate) {
            return solicitudesEmpate.contains(jugador.toUpperCase());
        }
    }

    public boolean registrarSolicitudEmpate(String jugador) {
        if (jugador == null)
            return false;
        synchronized (solicitudesEmpate) {
            if (solicitudesEmpate.isEmpty()) {
                primerProponenteEmpate = jugador;
            }
            solicitudesEmpate.add(jugador.toUpperCase());
            return solicitudesEmpate.size() >= 2;
        }
    }

    public void notificarPropuestaEmpate(String proponente) {
        if (proponente == null)
            return;
        String mensaje = "Server: " + proponente
            + " propone terminar en empate. Escribe 'DRAW' para aceptar o 'DRAW REJECT' para rechazar.";
        for (ServerThread client : connectedClients) {
            if (client == null || client.name == null)
                continue;
            if (client.name.equalsIgnoreCase(proponente))
                continue;
            try {
                Command aviso = CommandFactory.getCommand(new String[] { "RESULT", client.name, mensaje });
                client.objectSender.writeObject(aviso);
            } catch (IOException ignored) {
            }
        }
    }

    public void resolverEmpateMutuo() {
        this.refFrame.writeMessage("La partida terminó en empate mutuo.");
        for (ServerThread client : connectedClients) {
            if (client == null || client.name == null)
                continue;
            incrementarEmpateJugador(client.name);
            client.isActive = false;
            try {
                Command aviso = CommandFactory
                        .getCommand(new String[] { "RESULT", client.name, "La partida terminó en empate mutuo." });
                client.objectSender.writeObject(aviso);
            } catch (IOException ignored) {
            }
        }
        finalizarPartida();
    }

    public void rechazarSolicitudEmpate(String jugador) {
        ServerThread rechazador = obtenerJugador(jugador);
        if (rechazador == null) {
            return;
        }

        boolean habiaPropuesta;
        synchronized (solicitudesEmpate) {
            habiaPropuesta = !solicitudesEmpate.isEmpty();
            solicitudesEmpate.clear();
            primerProponenteEmpate = null;
        }

        if (!habiaPropuesta) {
            enviarResultadoPrivado(rechazador, "No hay propuestas de empate activas para rechazar.");
            return;
        }

        for (ServerThread client : connectedClients) {
            if (client == null || client.name == null)
                continue;
            String mensaje;
            if (client == rechazador) {
                mensaje = "Has rechazado la propuesta de empate.";
            } else {
                mensaje = "El jugador " + jugador + " rechazó la propuesta de empate.";
            }
            try {
                Command aviso = CommandFactory.getCommand(new String[] { "RESULT", client.name, mensaje });
                client.objectSender.writeObject(aviso);
            } catch (IOException ignored) {
            }
        }
    }

    public void limpiarSolicitudesEmpate() {
        synchronized (solicitudesEmpate) {
            solicitudesEmpate.clear();
        }
        primerProponenteEmpate = null;
    }
    
    // Funciones en relación a la recuperación de datos, ver https://www.w3schools.com/java/java_hashmap.asp

    public void ordenarStats() {  // Función encargada de ordenar las estadísticas en orden ascendente, en función del valor de WINS de un jugador
        List<Map.Entry<String, Stats>> entryList = new ArrayList<>(this.hashMapEstadisticas.entrySet());  // Se transforman las entradas del HashMap en una lista;
        // Aquí, estamos creando una lista que almacena <Map.Entry<String, Stats>>, reagrupando entonces todo el contenido del HashMap
        
        entryList.sort((entry1, entry2) -> // Ordenamos por WINS de forma ascendente
        Integer.compare(entry2.getValue().getHashMapStats().get("WINS"),
                entry1.getValue().getHashMapStats().get("WINS"))); // Esta línea indica al sort como ordenar, en función
                                                                   // de una regla de comparación
        // Debe retornar lo siguiente, al ejecutar una comparación del estilo (a,b) -> a
        // - b:
        // - NEGATIVO si a debe ir ANTES que b (si a = 5 y b = 10, 5 - 10 = -5, entonces
        // a va antes de b)
        // - CERO si son iguales en términos de orden (si a = 5 y b = 5, 5 - 5 = 0,
        // entonces el orden entre a y b da igual)
        // - POSITIVO si entry1 debe ir DESPUÉS que entry2 (si a = 15 y b = 10, 15 - 10
        // = 5, entonces a va después de b)
        
       this.hashMapEstadisticas.clear();  // Se limpia el mapa para volver a llenarlo
        for (Map.Entry<String, Stats> entry : entryList) {  // Recorremos la lista ordenada, añadiendo los elementos nuevamente en el HashMap
            this.hashMapEstadisticas.put(entry.getKey(), entry.getValue());
        }
    }

    
    public void iniciarStats() {  // Función para leer estadísticas de un archivo
    this.hashMapEstadisticas = new LinkedHashMap<>();  // Empezamos por un LinkedHashMap vacío
    if (!archivoEstadisticas.exists()) {  // Si no existe un archivo de estadísticas, se crea
        try {
            archivoEstadisticas.createNewFile();  // Creación del archivo
            System.out.println("Archivo de estadísticas creado nuevo");  // Realmente estamos guardando un archivo vacío: si antes no existían las estadísticas, no hay información que retomar
        } catch (IOException ex) {
            this.refFrame.writeMessage("Error creando archivo: " + ex.getMessage());
        }
    }

    try (FileReader reader = new FileReader(this.archivoEstadisticas)) {  // Si ya había algo, lo sacamos
        // Usamos GSON, una librería de Google para la lectura de JSON
        Gson gson = new Gson();
        Type type = new TypeToken<LinkedHashMap<String, Stats>>(){}.getType();  // Declaramos el tipo de dato que buscamos leer
        this.hashMapEstadisticas = gson.fromJson(reader, type);  // Leemos en el archivo que ya abrímos con anterioridad, almacenando su contenido en el HashMap (es posible, considerando que nuestro type es el mismo que el del HashMap)
        
        if (this.hashMapEstadisticas == null) {  // Si el resultado que tenemos es null (inválido), no lo tomamos en cuenta, retornando un HashMap vacío
            this.hashMapEstadisticas = new LinkedHashMap<>();
        }
        
        System.out.println("Estadísticas cargadas: " + this.hashMapEstadisticas.size() + " jugadores");  // Mensaje para debbug
        
        
    } catch (Exception e) {  // Secuencia de error al leer estadísticas
        System.out.println("Error leyendo estadísticas: " + e.getMessage());
        this.hashMapEstadisticas = new LinkedHashMap<>();  // El arreglo estaría mal, entonces se retorna vacío
    }
    ordenarStats();  // Ordenamos las stats
    // Prueba para verificar los datos leídos 
    this.hashMapEstadisticas.forEach((k, v) -> 
        System.out.println(k + ": " + v.toString())
    );
}

    public String textoMostrarRanking() {
        StringBuilder texto = new StringBuilder();
        texto.append("RANKING: \n");
        ArrayList<String> nombres = new ArrayList<String>(this.hashMapEstadisticas.keySet());
        for (int i = 0; i < this.hashMapEstadisticas.size(); i++) {
            String nombre = nombres.get(i);
            Stats stats = this.hashMapEstadisticas.get(nombre);
            texto.append((i + 1) + ". " + nombre + " - " + stats.toString() + "\n");
        }
        return texto.toString();
    }

    public String textoMostrarJugadorPropio(String nombreJugador) {
        StringBuilder texto = new StringBuilder();
        texto.append("MI ESTADO: \n");
        Stats estadisticas = this.hashMapEstadisticas.get(nombreJugador);
        if (estadisticas == null) {
            texto.append("(sin estadísticas)\n");
            return texto.toString();
        }
        estadisticas.getHashMapStats().forEach((k, v) -> {
            texto.append(k + ": " + v + "\n");
        });

        return texto.toString();
    }

    public String textoMostrarJugadorEnemigo(String nombreJugador) {
        StringBuilder texto = new StringBuilder();
        texto.append("ESTADO DE ")
                .append(nombreJugador)
                .append(": \n");
        Stats estadisticas = this.hashMapEstadisticas.get(nombreJugador);
        if (estadisticas == null) {
            texto.append("(sin estadísticas)\n");
            return texto.toString();
        }
        estadisticas.getHashMapStats().forEach((k, v) -> {
            texto.append(k + ": " + v + "\n");
        });

        return texto.toString();
    }
    
    public void crearNuevoJugador(String nombreJugador) {  // Si entra un jugador que no forma parte de los rankings, se añade
        this.hashMapEstadisticas.put(nombreJugador, new Stats());
        actualizarStats();  // Hubo cambios, se actualiza en archivo
    }
    
    public void incrementarStatJugador(String nombreJugador, String statIncrementada) {  // Se incrementa una estadística deseada del jugador indicado
        if (nombreJugador == null || statIncrementada == null)
            return;
        Stats statsJugador = this.hashMapEstadisticas.get(nombreJugador);  // Se consigue el arreglo de estadísticas del jugador
        if (statsJugador == null) {
            statsJugador = new Stats();
            this.hashMapEstadisticas.put(nombreJugador, statsJugador);
        }
        statsJugador.incrementarStat(statIncrementada);  // Se incrementa su valor
        actualizarStats();  // Se actualiza el archivo
    }

    public void aplicarResumenAtaque(String nombreJugador,
                                     int totalAtaques,
                                     int exitosos,
                                     int fallidos) {
        if (nombreJugador == null || nombreJugador.trim().isEmpty())
            return;
        Stats statsJugador = this.hashMapEstadisticas.get(nombreJugador);
        if (statsJugador == null) {
            statsJugador = new Stats();
            this.hashMapEstadisticas.put(nombreJugador, statsJugador);
        }
        statsJugador.addAttackSummary(Math.max(0, totalAtaques),
                Math.max(0, exitosos),
                Math.max(0, fallidos));
        actualizarStats();
    }

    public void incrementarEmpateJugador(String nombreJugador) {
        if (nombreJugador == null || nombreJugador.trim().isEmpty())
            return;
        Stats statsJugador = this.hashMapEstadisticas.get(nombreJugador);
        if (statsJugador == null) {
            statsJugador = new Stats();
            this.hashMapEstadisticas.put(nombreJugador, statsJugador);
        }
        LinkedHashMap<String, Integer> mapa = statsJugador.getHashMapStats();
        if (!mapa.containsKey("DRAWS")) {
            mapa.put("DRAWS", 0);
        }
        mapa.put("DRAWS", mapa.get("DRAWS") + 1);
        actualizarStats();
    }

    public void actualizarStats() {  // Función para guardar actualizaciones en el archivo (cada que hay un cambio): se usa la líbrería GSON
        Gson gson = new Gson();  // Se crea un nuevo Objeto Gson
        Type typeObject = new TypeToken<LinkedHashMap<String, Stats>>() {}.getType();  // Define el tipo para la conversión: HashMap<String, TipoEstadistica>
        String gsonData = gson.toJson(this.hashMapEstadisticas, typeObject);  // Serializa el diccionario this.hashMapEstadisticas a formato JSON, siguiendo el tipo indicado
        
        try (FileWriter writer = new FileWriter(this.archivoEstadisticas, false)) {  // Intentamos crear una escritor en el archivo. El 'false' borra todo el contenido que estaba anteriormente
            writer.write(gsonData);
            ordenarStats();  // Procedemos a ordenar las stats
            actualizarDatosParaJugadores();
        } catch (IOException e) {  // Si hay una excepción al intentar escribir
            System.out.println("No se puede escribir en el archivo");  // Si llega a existir un error al intentar escribir, se muestra en consola
        }
    }

    public void actualizarDatosParaJugadores() {
        String ranking = textoMostrarRanking();

        for (ServerThread client : connectedClients) {
            try {
                if (client.name == null)
                    continue;

                String ownStats = "";
                if (this.hashMapEstadisticas.containsKey(client.name)) {
                    ownStats = textoMostrarJugadorPropio(client.name);
                }

                String enemyStats = "";
                if (client.ultimoEnemigoAtacado != null &&
                        !client.ultimoEnemigoAtacado.isEmpty() &&
                        this.hashMapEstadisticas.containsKey(client.ultimoEnemigoAtacado)) {
                    enemyStats = textoMostrarJugadorEnemigo(client.ultimoEnemigoAtacado);
                }

                CommandUpdateStats statsCmd = new CommandUpdateStats(ranking, ownStats, enemyStats);
                client.objectSender.writeObject(statsCmd);
            } catch (IOException ex) {
                System.out.println("Error enviando stats a " + client.name + ": " + ex.getMessage());
            }
        }
    }

    /**
     * Envía estadísticas actualizadas a un jugador específico.
     * Incluye ranking, stats propias y stats del enemigo especificado.
     */
    public void enviarStatsAJugador(String nombreJugador, String nombreEnemigo) {
        if (nombreJugador == null)
            return;

        String ranking = textoMostrarRanking();
        String ownStats = "";
        String enemyStats = "";

        if (this.hashMapEstadisticas.containsKey(nombreJugador)) {
            ownStats = textoMostrarJugadorPropio(nombreJugador);
        }

        if (nombreEnemigo != null && !nombreEnemigo.isEmpty() && this.hashMapEstadisticas.containsKey(nombreEnemigo)) {
            enemyStats = textoMostrarJugadorEnemigo(nombreEnemigo);
        }

        CommandUpdateStats statsCmd = new CommandUpdateStats(ranking, ownStats, enemyStats);
        enviarComandoAJugador(nombreJugador, statsCmd);
    }

    private void manejarSeleccionJugador(ServerThread origin, CommandSelectPlayer comando) {
        if (origin == null || comando == null)
            return;
        if (origin.name == null || origin.name.trim().isEmpty())
            return;

        String target = comando.getTargetPlayer();
        if (target == null || target.trim().isEmpty()) {
            enviarResultadoPrivado(origin, "Debe indicar el nombre del jugador a consultar.");
            return;
        }

        String nombreExacto = buscarNombreEnEstadisticas(target);
        if (nombreExacto == null) {
            enviarResultadoPrivado(origin, "No se encontraron estadísticas para '" + target + "'.");
            return;
        }

        origin.ultimoEnemigoAtacado = nombreExacto;
        enviarStatsAJugador(origin.name, nombreExacto);
    }

    public void executeCommand(Command comando, ServerThread origin) {

        if (comando.getType() == CommandType.SELECTPLAYER) {
            if (comando instanceof CommandSelectPlayer) {
                manejarSeleccionJugador(origin, (CommandSelectPlayer) comando);
            }
            return;
        }

        if (comando.getType() == CommandType.SKIP) {
            nextTurn();
        }

        // Reenviar el comando según su tipo de difusión
        // Si es broadcast true
        if (comando.getIsBroadcast()) {
            this.getRefFrame().writeMessage("Actividad broadcast del jugador " + origin.name + "(ver cliente)");
            this.broadcast(comando);
        }

        // Si es comando propio
        else if (comando.isOwnCommand()) {
            processPrivate(comando, origin);
        }
            

            // Si es uno que se refleja en cliente enemigo
        else if (comando.getParameters().length > 1 && this.buscarJugador(comando.getParameters()[1])) { // Enviar privado
            this.sendPrivate(comando);
        }

            // Si no se encuentra receptor
        else {
            String[] args = new String[] { "RESULT", "Server: Jugador objetivo no encontrado" };
            try {
                origin.objectSender.writeObject(CommandFactory.getCommand(args));
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        // Después de procesar el comando, comprobar condición de victoria
        try {
            GameVictoryManager.checkVictory(this);
        } catch (Exception ignored) {
        }

        // Actualizar stats para todos los jugadores después de cada comando
        actualizarDatosParaJugadores();

    }

    public void broadcast(Command comando) {
        for (ServerThread client : connectedClients) {
            try {
                client.objectSender.writeObject(comando);
            } catch (IOException ex) {

            }
        }

    }

    public void processPrivate(Command comando, ServerThread own) {
        try {
            own.objectSender.writeObject(comando);

        } catch (IOException ex) {

        }
    }

    /**
     * Envía un comando a un jugador específico por nombre.
     */
    public void enviarComandoAJugador(String nombreJugador, Command comando) {
        if (nombreJugador == null || comando == null)
            return;

        for (ServerThread client : connectedClients) {
            if (client.name != null && client.name.equalsIgnoreCase(nombreJugador)) {
                try {
                    client.objectSender.writeObject(comando);
                } catch (IOException ex) {
                    System.out.println("Error enviando comando a " + nombreJugador + ": " + ex.getMessage());
                }
                break;
            }
        }
    }

    public void enviarResultadoPrivado(ServerThread origin, String mensaje) {
        if (origin == null || origin.name == null || mensaje == null || mensaje.trim().isEmpty())
            return;
        String[] args = new String[] { "RESULT", origin.name, mensaje };
        try {
            origin.objectSender.writeObject(CommandFactory.getCommand(args));
        } catch (IOException ignored) {
        }
    }

    private String buscarNombreEnEstadisticas(String candidato) {
        if (candidato == null)
            return null;
        String buscado = candidato.trim();
        if (buscado.isEmpty())
            return null;
        for (String nombre : this.hashMapEstadisticas.keySet()) {
            if (nombre != null && nombre.equalsIgnoreCase(buscado)) {
                return nombre;
            }
        }
        return null;
    }

    public void sendPrivate(Command comando) {
        // asumo que el nombre del cliente viene en la posición 1 . private_message
        // Andres "Hola"
        if (comando.getParameters().length <= 1)
            return;

        String searchName = comando.getParameters()[1].toUpperCase();

        for (ServerThread client : connectedClients) {
            if (client.name.toUpperCase().equals(searchName)) {
                try {
                    client.objectSender.writeObject(comando);
                    break;
                } catch (IOException ex) {

                }
            }
        }
    }

    // Probablemente no sea la mejor implementacion :(
    public void nextTurn() {

        for (ServerThread client : connectedClients) {
            if (client.isTurn) { // Buscar el del turno actual

                client.setIsTurn(false); // Quitarle el turno
                int indice = connectedClients.indexOf(client); // Obtener indice

                if (indice + 1 >= connectedClients.size()) { // Era el ultima de la lista?
                    connectedClients.getFirst().setIsTurn(true); // Si, dele el turno al primero

                    if (connectedClients.getFirst().isActive) { // Esta activo?
                        // SI, entonces imprimir de quien es el turno y terminar
                        this.refFrame.writeMessage("Turno de: " + connectedClients.getFirst().name);
                        return;
                    } else {
                        // No, entonces repetir el proceso con el nuevo que tiene un turno
                        nextTurn();
                        return;
                    }
                }
                connectedClients.get(indice + 1).setIsTurn(true); // No, dele el turno al sigt de la lista

                if (connectedClients.get(indice + 1).isActive) { // Esta activo?
                    // Si, entonces imprimir de quien es el turno y terminar
                    this.refFrame.writeMessage("Turno de: " + connectedClients.get(indice + 1).name);
                    return;
                } else {
                    // no, repetir y terminar
                    nextTurn();
                    return;
                }
            }
        }
        // Si no encuentra a nadie con el isTurn=true, darselo al primero (significa que
        // es el inicio de la partida)
        connectedClients.getFirst().setIsTurn(true);
        this.refFrame.writeMessage("Turno de: " + connectedClients.getFirst().name);
    }

    public boolean buscarJugador(String searchName) {
        for (ServerThread client : connectedClients) {
            if (client.name != null && client.name.equalsIgnoreCase(searchName))
                return true;
        }
        return false;
    }

    public int contarJugadoresActivos() {
        int activos = 0;
        for (ServerThread client : connectedClients) {
            if (client != null && client.isActive) {
                activos++;
            }
        }
        return activos;
    }

    public ServerThread obtenerJugador(String playerName) {  //Obtiene el serverthread asignado al jugador
        if (playerName == null) {
            return null;
        }
        for (ServerThread client : connectedClients) {
            if (client.name != null && client.name.equalsIgnoreCase(playerName)) {
                return client;
            }
        }
        return null;
    }

    public boolean isNameTaken(String candidateName, ServerThread requester) {
        if (candidateName == null)
            return false;
        for (ServerThread client : connectedClients) {
            if (client == null || client == requester) // En caso de que se verifique el mismo cliente
                continue;
            if (client.name != null && client.name.equalsIgnoreCase(candidateName)) // Verificar con distintos clientes,
                                                                                    // distinto nombre
                return true;
        }
        return false;
    }

    public void declareActiveClients() {
        for (ServerThread client : connectedClients) {
            client.isActive = true;
        }
    }

    public void showAllNames() {
        this.refFrame.writeMessage("Usuarios conectados");
        for (ServerThread client : connectedClients) {
            this.refFrame.writeMessage(client.name);
        }
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public LinkedList<ServerThread> getConnectedClients() {
        return connectedClients;
    }

    public ServerFrame getRefFrame() {
        return refFrame;
    }

    public File getArchivoEstadisticas() {
        return archivoEstadisticas;
    }

    public LinkedHashMap<String, Stats> getHashMapEstadisticas() {
        return hashMapEstadisticas;
    }

    // Exponer estado de inicio de partida
    public boolean getStart() {
        return start;
    }

    public void finalizarPartida() {
        this.start = false;
        limpiarSolicitudesEmpate();
    }
}
