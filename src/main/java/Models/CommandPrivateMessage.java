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
public class CommandPrivateMessage extends Command{

    public CommandPrivateMessage(String[] args) {
        super(CommandType.PRIVATE_MESSAGE, args);
        this.consumesTurn = false;
        this.ownCommand = false;
    }

    @Override
    public void processForServer(ServerThread serverThread) {
        this.setIsBroadcast(false);
    }
    
    @Override
    public void processInClient(Client client) {
        //private_message Andres "Hola mundo"
        // client.getRefFrame().writeMessage("Mensaje para " + this.getParameters()[1] + ": " + this.getParameters()[2]);
        }
    }
