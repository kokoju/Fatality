/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;

import Servidor.ThreadServidor;

/**
 *
 * @author sando
 */
public class CommandNextTurn extends Command {
    
    public CommandNextTurn(String[] args) {
        super(CommandType.NEXT, args);
        this.consumesTurn = false;
        this.ownCommand = false;
    }

    @Override
    public void processForServer(ThreadServidor threadServidor) {
        this.setIsBroadcast(true);
        threadServidor.getServer().nextTurn();
    }
    
    @Override
    public String toString() {
        return "Siguiente turno...";
    }
}
