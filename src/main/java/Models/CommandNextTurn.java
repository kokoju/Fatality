/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;

import Server.ServerThread;

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
    public void processForServer(ServerThread serverThread) {
        this.setIsBroadcast(true);
        serverThread.getServer().nextTurn();
    }
    
    @Override
    public String toString() {
        return "Siguiente turno...";
    }
}
