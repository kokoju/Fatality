/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;

import Client.Client;
import Server.Server;
import Server.ServerThread;
import java.io.IOException;

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
        String requestedName = getParameters()[1].trim();
        Server server = serverThread.getServer();

        if (requestedName.isEmpty()) {
            notifyHandshake(serverThread, requestedName, "El nombre no puede estar vacío.");
            return;
        }

        if (server.isNameTaken(requestedName, serverThread)) {
            notifyHandshake(serverThread, requestedName, "Ese nombre ya está en uso.");
            return;
        }

        this.setIsBroadcast(true);
        serverThread.name = requestedName;
        serverThread.showAllClients();
                      
        if (!serverThread.getServer().getHashMapEstadisticas().containsKey(serverThread.name))  // Si el cliente no era parte del arreglo de datos, se le genera un espacio en el archivo
            serverThread.getServer().crearNuevoJugador(serverThread.name);
    }
    
    @Override
    public void processInClient(Client client) {
        //NAME Nombre de persona
        client.getRefFrame().writeMessage("Conectado el cliente: " + this.getParameters()[1]);
         
    }

    private void notifyHandshake(ServerThread serverThread, String requestedName, String reason) {
        String[] args = new String[]{"NAME_HANDSHAKE", requestedName, reason};
        try {
            serverThread.objectSender.writeObject(CommandFactory.getCommand(args));
        } catch (IOException ex) {
            serverThread.getServer().getRefFrame().writeMessage("No se pudo enviar handshake de nombre: " + ex.getMessage());
        }
    }

}
