/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;

import Cliente.Client;
import Servidor.ThreadServidor;

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
    public void processForServer(ThreadServidor threadServidor) {
        this.setIsBroadcast(true);
        threadServidor.isActive = false;

        // Verificar si hay un ganador después de marcar como inactivo
        try {
            Servidor.GameVictoryManager.checkVictory(threadServidor.getServer());
        } catch (Exception ignored) {
        }
    }

    @Override
    public String toString() {
        return "El jugador ha decidido rendirse";
    }
}
