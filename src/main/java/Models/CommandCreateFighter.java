/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;

import Client.Arma;
import Client.Client;
import Client.Jugador;
import Peleador.Peleador;
import Peleador.Tipo;
import Server.ServerThread;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles CreateFighter <Nombre> <Tipo> requests.
 */
public class CommandCreateFighter extends Command {

    private static final String[] ARMAS_DISPONIBLES = {
            "Espada", "Arco", "Hacha", "Lanza", "Martillo",
            "Daga", "Escudo", "Ballesta", "Katana", "Maza",
            "Florete", "Guadaña", "Tridente", "Shuriken", "Nunchaku"
    };

    public CommandCreateFighter(String[] args) {
        super(CommandType.CREATEFIGHTER, args);
        this.consumesTurn = false;
        this.ownCommand = true;
        this.setIsBroadcast(false);
    }

    @Override
    public void processForServer(ServerThread serverThread) {
        // Este comando se maneja exclusivamente en el cliente.
    }

    @Override
    public void processInClient(Client client) {
        if (client == null)
            return;
        Jugador jugador = client.getJugador();
        if (jugador == null) {
            client.getRefFrame().writeMessage("Aún no tienes un jugador inicializado");
            return;
        }

        String[] params = getParameters();
        if (params == null || params.length < 3) {
            client.getRefFrame().writeMessage("Uso: CreateFighter <Nombre> <Tipo>");
            return;
        }

        String fighterName = params[1] == null ? "" : params[1].trim();
        if (fighterName.isEmpty()) {
            client.getRefFrame().writeMessage("El nombre del peleador no puede estar vacío");
            return;
        }

        // Validar contra la lista oficial de peleadores permitidos
        String[] permitidos = new String[] {
                "black_manta",
                "omni_man",
                "peacemaker",
                "red_hood",
                "robocop",
                "scarecrow"
        };
        String nombreNormalizado = fighterName.trim().toLowerCase();
        boolean esPermitido = java.util.Arrays.stream(permitidos).anyMatch(p -> p.equals(nombreNormalizado));
        if (!esPermitido) {
            client.getRefFrame().writeMessage("Nombre inválido. Permitidos: " + String.join(", ", permitidos));
            return;
        }

        String tipoRaw = params[2] == null ? "" : params[2].trim();
        Tipo tipo;
        try {
            tipo = Tipo.valueOf(tipoRaw.toUpperCase());
        } catch (IllegalArgumentException ex) {
            client.getRefFrame().writeMessage("Tipo inválido. Valores permitidos: " + availableTypes());
            return;
        }

        // Verificar límite de 4 peleadores antes de crear
        if (jugador.getCantidadPeleadores() >= 4) {
            client.getRefFrame().writeMessage("No puedes crear más de 4 luchadores por cliente");
            return;
        }

        if (jugador.tienePeleador(fighterName)) {
            client.getRefFrame().writeMessage("Ya tienes un peleador llamado '" + fighterName + "'");
            return;
        }

        boolean registrado = jugador.registrarNombrePeleador(fighterName, tipo);
        if (!registrado) {
            client.getRefFrame().writeMessage("No se pudo registrar el peleador (lista llena o duplicado)");
            return;
        }

        // Asignar 5 armas aleatorias al peleador recién creado
        Peleador peleador = jugador.buscarPeleadorPorNombre(fighterName);
        if (peleador != null) {
            asignarArmasAleatorias(peleador);
        }

        // Actualizar imagen en el pnlMenu del FrameClient
        try {
            client.getRefFrame().establecerImagenLuchador(fighterName);
        } catch (Exception e) {
            client.getRefFrame().writeMessage("No se pudo cargar la imagen para '" + fighterName + "'");
        }

        client.getRefFrame().writeMessage("Peleador '" + fighterName + "' creado como " + tipo.toString());
    }

    private String availableTypes() {
        return Arrays.stream(Tipo.values())
                .map(Tipo::name)
                .collect(Collectors.joining(", "));
    }

    private void asignarArmasAleatorias(Peleador peleador) {
        List<String> pool = new ArrayList<>(Arrays.asList(ARMAS_DISPONIBLES));
        Collections.shuffle(pool);
        int asignadas = 0;
        for (String nombreArma : pool) {
            if (asignadas >= 5)
                break;
            Arma arma = new Arma(nombreArma);
            if (peleador.asignarArma(arma)) {
                asignadas++;
            }
        }
    }
}
