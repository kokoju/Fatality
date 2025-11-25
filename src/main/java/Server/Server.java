/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import Models.Command;
import Models.CommandFactory;
import Models.CommandType;
import Models.CommandApplyAttack;
import Models.AttackPayload;
import Server.ServerFrame;
import Servidor.ConnectionThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author diego
 */
public class Server {
    private final int PORT = 35500;
    private final int maxConections = 4;
    private ServerSocket serverSocket;
    private LinkedList<ServerThread> connectedClients; // arreglo de hilos por cada cliente conectado
    // referencia a la pantalla
    private ServerFrame refFrame;
    private ConnectionThread connectionsThread;

    // Juego iniciado?
    private boolean start = false;

    public Server(FrameServer refFrame) {
        connectedClients = new LinkedList<ThreadServidor>();
        this.refFrame = refFrame;
        this.init();
        connectionsThread = new ThreadConnections(this);
        connectionsThread.start();
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

    void executeCommand(Command comando, ServerThread origin) {
        // Si es un ApplyyAttack, validar el payload antes de reenviar
        try {
            if (comando.getType() == CommandType.APPLYATTACK) {

                if (comando instanceof CommandApplyAttack) {

                    CommandApplyAttack ca = (CommandApplyAttack) comando;
                    AttackPayload payload = ca.getPayload();

                    if (payload != null && payload.getHeroPackage() != null) {
                        Hero prueba = HeroFactory.createFromPackage(payload.getHeroPackage());
                        if (prueba == null) {

                            // Noticar al que envia el ataque que hubo un error utilizando RESULT
                            String msg = "Server: invalid HeroPackage in attack from '" + payload.getAttackerName()
                                    + "'";
                            String[] args = new String[] { "RESULT", origin.name, msg };

                            try {
                                origin.objectSender.writeObject(CommandFactory.getCommand(args));
                            } catch (IOException e) {
                                // ignore
                            }
                            return;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            // validation failure; notificar al server
            try {
                String[] args = new String[] { "RESULT", origin.name, "Server: error validating attack payload" };
                origin.objectSender.writeObject(CommandFactory.getCommand(args));
            } catch (IOException e) {
            }
            return;
        }

        if (comando.getType() == CommandType.SKIP) {
            nextTurn();
        }

        // Reenviar el comando según su tipo de difusión -> PERDON :(
        // Si es broadcast true
        if (comando.getIsBroadcast()) {
            this.getRefFrame().writeMessage("Actividad broadcast del jugador " + origin.name + "(ver cliente)");
            this.broadcast(comando);
        }

        // Si es comando propio
        else if (comando.isOwnCommand()) {
            processPrivate(comando, origin);

            // Si es uno que se refleja en cliente enemigo
        } else if (comando.getParameters().length > 1 && this.buscarJugador(comando.getParameters()[1])) { // Enviar
                                                                                                           // privado
            this.sendPrivate(comando);

            // Si no se encuentra receptor
        } else {
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
            if (client.name.equalsIgnoreCase(searchName))
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

    public int getMaxConections() {
        return maxConections;
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

    // Exponer estado de inicio de partida
    public boolean getStart() {
        return start;
    }

}
