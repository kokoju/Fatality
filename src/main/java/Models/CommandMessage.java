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
public class CommandMessage extends Command{

    public CommandMessage(String[] args) {
        super(CommandType.MESSAGE, args);
        this.consumesTurn = false;
        this.ownCommand = false;

    }

    @Override
    public void processForServer(ServerThread serverThread) {
        this.setIsBroadcast(true);
        
    }
    
    @Override
    public void processInClient(Client client) {
        //Message "string"
        client.getRefFrame().writeMessage("Mensaje recibido: " + this.getParameters()[1]);
    }
    
}
