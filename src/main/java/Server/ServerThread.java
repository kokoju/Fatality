/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import Models.Command;
import Models.CommandFactory;
import Models.CommandResult;
import Models.CommandType;
import static Models.CommandType.*;
import Server.Server;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 *
 * @author diego
 */
public class ServerThread extends Thread {
    private Server server;
    private Socket socket;
    //Streams para leer y escribir objetos
    public ObjectInputStream objectListener;
    public ObjectOutputStream objectSender;
    public String name;
    
    public boolean isActive = false;
        
    public boolean isRunning = true;
    
    public boolean isTurn = false;
    
    public boolean isReady = false;
    
    
    

    public ServerThread(Server server, Socket socket) {
        try {
        this.server = server;
        this.socket = socket;
        objectSender =  new ObjectOutputStream (socket.getOutputStream());
        objectSender.flush();
        objectListener =  new ObjectInputStream (socket.getInputStream());
        } catch (IOException ex) {
                System.out.println(ex.getMessage());
        } catch (Exception ex){
            System.out.println(ex.getMessage());
        }
        
    }
    
    public void run (){
        Command comando;
        while (isRunning){
            try {
                comando = (Command)objectListener.readObject();
                server.getRefFrame().writeMessage("ThreadServer recibió: " + comando);
                comando.processForServer(this);
                
                //Si el comando consume el turno y no es el turno del cliente
                if(comando.isConsumesTurn() && !this.isTurn) {   
                    String [] args = new String[]{"RESULT","No es su turno para jugar"};
                    this.objectSender.writeObject(CommandFactory.getCommand(args));
                   
                //Si el jugador esta vivo
                } else if (isActive) {
                    server.executeCommand(comando, this);
                } else if (this.isTurn)
                    server.nextTurn();
                        
                
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            } catch (ClassNotFoundException ex) {
                System.out.println(ex.getMessage());
            }  
        } 
    }

    public void showAllClients (){
        this.server.showAllNames();
    }

    public void setIsTurn(boolean isTurn) {
        this.isTurn = isTurn;
    }
    
    public void setIsActive(boolean isActive){
        this.isActive = isActive;
    }

    public Server getServer() {
        return server;
    }

    public String getClientName() {
        return name;
    }
}
