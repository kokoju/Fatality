/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

import Client.Client;
import Models.Command;
import Server.Server;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author diego
 */
public class ClientThread extends Thread{
    private Client client;
    
    private boolean isRunning = true;
    // Bitácora local al thread (guarda eventos relevantes para este cliente)
    private ArrayList<String> bitacora;

    public void addBitacora(String entrada) {
        if (bitacora == null) 
            bitacora = new ArrayList<>();
        bitacora.add(entrada);
    }

    public ArrayList<String> getBitacora() {
        if (bitacora == null) 
            bitacora = new ArrayList<>();
        return bitacora;
    }

    public ClientThread(Client client) {
        this.client = client;

        this.bitacora = new ArrayList<>();

    }
    
    public void run (){
        Command comandoRecibido;
        while (isRunning){
            try {
                comandoRecibido = (Command) client.objectListener.readObject();
                //receivedMessage = client.getListener().readUTF(); //espera hasta recibir un String desde el cliente que tiene su socket
                comandoRecibido.processInClient(client);
            } catch (IOException ex) {
                
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }
        
    }
}
