/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import Server.Server;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author diego
 */
public class ConnectionThread extends Thread {
    private Server server;

    public ConnectionThread(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        Socket newSocket = null;
        server.getRefFrame().writeMessage("Esperando conexiones");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                newSocket = server.getServerSocket().accept();
                //una vez con socket recibido, se crea e inicia el thread que va a anteder y escuchar al clinete que los conectó
                ServerThread newServerThread = new ServerThread(server, newSocket);
                server.getConnectedClients().add (newServerThread);
                
                newServerThread.start();

                server.getRefFrame().writeMessage("Cliente conectado");
            
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
                //server.getRefFrame().writeMessage("Error: " +  ex.getMessage());
            } 
        }
    }
    
}
