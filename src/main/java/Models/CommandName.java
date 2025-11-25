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
public class CommandName extends Command{
    
    public CommandName(String[] args) { //name Diego
        super(CommandType.NAME, args);
        this.consumesTurn = false;
        this.ownCommand = true;
    }

    @Override
    public void processForServer(ThreadServidor threadServidor) {
        this.setIsBroadcast(true);
        threadServidor.name = getParameters()[1];
        threadServidor.showAllClients();
    }
    
    @Override
    public void processInClient(Client client) {
        //NAME Nombre de persona
        client.getRefFrame().writeMessage("Conectado el cliente: " + this.getParameters()[1]);
        
        
    }

}
