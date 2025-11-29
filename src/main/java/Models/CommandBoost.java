/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;

import Client.Client;
import Hero.Hero;
import Server.ServerThread;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sando
 */
public class CommandBoost extends Command {
    
    public CommandBoost(String[] args) {
        super(CommandType.BOOST, args);
        this.consumesTurn = true;
        this.ownCommand = true;
    }

    @Override
    public void processForServer(ServerThread serverThread) {
        this.setIsBroadcast(false);
        
    }
    
    @Override
    public void processInClient(Client client) { //Cliente propio
        //Forma: BOOST <Heroe> <HEAL/PROTECT/STRENGTHEN>
        
        //Buscar heroe, si no existe, mensaje de error
        String heroeName = this.getParameters()[1].toUpperCase();
        String tipoBoost = this.getParameters()[2].toUpperCase();
        Hero heroe = client.getJugador().buscarHeroe(heroeName);
        boolean flag = false;
        
        client.getJugador().deshabilitarResistencias();
                
        if(heroe == null) {
            client.getRefFrame().writeMessage("Heroe seleccionado no existe: " + heroeName);
            flag = true;
        } 
        //Activa el boosteo y regresa el true, si no lo encuentra obviamente no activa nada y regresa false
        else if(!heroe.activarBoost(tipoBoost)) {  
                client.getRefFrame().writeMessage("Mejora seleccionada no existe: " + tipoBoost);
                flag = true;
        }  
        
        if(!flag) {
            client.getRefFrame().writeMessage("El jugador " + client.name + " ha usado " + this.getParameters()[1] + " para ayudar a su civilizacion con " + this.getParameters()[2]);
            try {
                client.objectSender.writeObject(CommandFactory.getCommand(new String[]{"NEXT"}));
            } catch (IOException ex) {
                Logger.getLogger(CommandBoost.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
}

