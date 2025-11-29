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
public class CommandReady extends Command{
    
    public CommandReady(String[] args) {
        super(CommandType.READY, args);
        this.consumesTurn = false;
        this.ownCommand = false;

    }

    @Override
    public void processForServer(ServerThread serverThread) {
        this.setIsBroadcast(true);
        serverThread.getServer().startGame(serverThread);
    }
    
}
