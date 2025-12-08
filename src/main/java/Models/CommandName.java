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
public class CommandName extends Command{
    
    public CommandName(String[] args) { //name Diego
        super(CommandType.NAME, args);
        this.consumesTurn = false;
        this.ownCommand = true;
    }

    @Override
    public void processForServer(ServerThread serverThread) {
        this.setIsBroadcast(true);
        serverThread.name = getParameters()[1];
        serverThread.showAllClients();
                      
        if (!serverThread.getServer().getHashMapEstadisticas().containsKey(serverThread.name))  // Si el cliente no era parte del arreglo de datos, se le genera un espacio en el archivo
            serverThread.getServer().crearNuevoJugador(serverThread.name);
    }
    
    @Override
    public void processInClient(Client client) {
        //NAME Nombre de persona
        //client.getRefFrame().writeMessage("Conectado el cliente: " + this.getParameters()[1]);
         
    }

}
