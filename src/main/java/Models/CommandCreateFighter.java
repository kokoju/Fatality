/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;

import Client.Client;
import Client.Jugador;
import Peleador.Tipo;
import Server.ServerThread;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Handles CreateFighter <Nombre> <Tipo> requests.
 */
public class CommandCreateFighter extends Command {

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

        String tipoRaw = params[2] == null ? "" : params[2].trim();
        Tipo tipo;
        try {
            tipo = Tipo.valueOf(tipoRaw.toUpperCase());
        } catch (IllegalArgumentException ex) {
            client.getRefFrame().writeMessage("Tipo inválido. Valores permitidos: " + availableTypes());
            return;
        }

        // TODO: validar que fighterName exista dentro de la lista oficial de peleadores generales

        if (jugador.tienePeleador(fighterName)) {
            client.getRefFrame().writeMessage("Ya tienes un peleador llamado '" + fighterName + "'");
            return;
        }

        boolean registrado = jugador.registrarNombrePeleador(fighterName, tipo);
        if (!registrado) {
            client.getRefFrame().writeMessage("No se pudo registrar el peleador (lista llena o duplicado)");
            return;
        }

        client.getRefFrame().writeMessage("Peleador '" + fighterName + "' creado como " + tipo.toString());
    }


    private String availableTypes() {
        return Arrays.stream(Tipo.values())
                .map(Tipo::name)
                .collect(Collectors.joining(", "));
    }
}
