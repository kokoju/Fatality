/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;

import Client.Client;
import Server.ServerThread;

/**
 *
 * @author diego
 */
public class CommandGiveup extends Command {

    public CommandGiveup(String[] args) {
        super(CommandType.PRIVATE_MESSAGE, args);
        this.consumesTurn = false;
        this.ownCommand = true;

    }

    @Override
    public void processForServer(ServerThread serverThread) {
        this.setIsBroadcast(true);
        serverThread.isActive = false;

        // Verificar si hay un ganador después de marcar como inactivo
        try {
            Server.GameVictoryManager.checkVictory(serverThread.getServer());
        } catch (Exception ignored) {
        }
    }

    @Override
    public String toString() {
        return "El jugador ha decidido rendirse";
    }
}
