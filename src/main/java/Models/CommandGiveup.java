/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;

import Client.Client;
import Server.GameVictoryManager;
import Server.Server;
import Server.ServerThread;
import java.io.IOException;

/**
 *
 * @author diego
 */
public class CommandGiveup extends Command {

    public CommandGiveup(String[] args) {
        super(CommandType.GIVEUP, args);
        this.consumesTurn = false;
        this.ownCommand = false;
        this.setIsBroadcast(false);
    }

    @Override
    public void processForServer(ServerThread serverThread) {
        this.setIsBroadcast(false);
        if (serverThread == null)
            return;

        Server server = serverThread.getServer();
        String surrenderName = serverThread.name;
        boolean teniaTurno = serverThread.isTurn;
        serverThread.isActive = false;

        if (server != null && surrenderName != null) {
            server.incrementarStatJugador(surrenderName, "GIVEUP");
            server.getRefFrame().writeMessage("Jugador " + surrenderName + " se rindió.");
            serverThread.setIsTurn(false);

            int activosRestantes = server.contarJugadoresActivos();
            if (teniaTurno && activosRestantes > 1) {
                server.nextTurn();
            }
        }

        notificarJugador(serverThread);

        try {
            GameVictoryManager.checkVictory(server);
        } catch (Exception ignored) {
        }
    }

    @Override
    public String toString() {
        return "El jugador ha decidido rendirse";
    }

    private void notificarJugador(ServerThread serverThread) {
        if (serverThread == null || serverThread.objectSender == null || serverThread.name == null)
            return;
        try {
            Command resultado = CommandFactory.getCommand(
                    new String[] { "RESULT", serverThread.name, "Te has rendido. Espera el resultado final." });
            serverThread.objectSender.writeObject(resultado);
        } catch (IOException ignored) {
        }
    }
}
