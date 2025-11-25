/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;

import Cliente.Client;
import Cliente.Jugador;
import Hero.Hero;
import Hero.HeroPackage;
import Servidor.ThreadServidor;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author diego
 */
public class CommandAttack extends Command {

    public CommandAttack(String[] args) { // ATTACK Andres 5 7
        super(CommandType.ATTACK, args);
        this.consumesTurn = false;
        this.ownCommand = true;
    }

    @Override
    public void processForServer(ThreadServidor threadServidor) {
        this.setIsBroadcast(false);
    }

    @Override
    public void processInClient(Client cliente) { // Cliente

        Command sendComando;
        boolean flag = false; // Indica si se detecta un error
        Jugador atacante = cliente.getJugador();

        // Validar que el jugador local esté inicializado
        if (atacante == null) {
            if (cliente.getRefFrame() != null)
                cliente.getRefFrame().writeMessage("Imposible realizar ataque: jugador local no inicializado");
            return;
        }

        String[] params = this.getParameters();
        if (params == null || params.length < 4) {
            cliente.getRefFrame().writeMessage("Parámetros insuficientes para ATTACK");
            return;
        }

        Hero heroeAtacante = atacante.buscarHeroe(params[2]);
        // Ver si el heroe existe
        if (heroeAtacante == null) {
            cliente.getRefFrame().writeMessage("El heroe escrito no existe");
            return;
            
        // Ver si el ataque y parametros extra son correctos
        } else {
        
            try {
                heroeAtacante.setMatrizAtaque(atacante.getMatriz());
            } catch (Exception ignore) {
        }
        
        
        
        if (!heroeAtacante.buscarHeroes(params[3])) {   // indice 3 deberia contener el ataque
            if (params[3].equalsIgnoreCase("ControlTheKraken"))
                cliente.getRefFrame().writeMessage("Este ataque se encuentra activo de forma pasiva");
            else 
                cliente.getRefFrame().writeMessage("El ataque escrito no existe");
            return;
        }
    }
        
        cliente.getJugador().deshabilitarResistencias();

        // Construir payload con HeroPackage
        String attackerName = cliente.name;
        String targetName = params[1];
        // Reconstruir el HeroPackage antes de decidir el heroType
        HeroPackage hp = null;
        if (atacante != null) hp = atacante.buildHeroPackage(params[2]);
        // heroType debe ser el tipo usado por HeroFactory; si no está disponible, usar el nombre de la clase del héroe
        String heroType = (hp != null && hp.getHeroType() != null) ? hp.getHeroType() : (heroeAtacante != null ? heroeAtacante.getClass().getSimpleName().toUpperCase() : params[2].toUpperCase());
        String attackType = params[3];
        String[] extras = new String[params.length - 4];
        for (int i = 4; i < params.length; i++)
            extras[i - 4] = params[i];


        AttackPayload payload = new AttackPayload(attackerName, targetName, heroType, params[2],attackType, extras, hp);
        sendComando = new CommandApplyAttack(payload);

        try {
            cliente.objectSender.writeObject(sendComando);
        } catch (IOException ex) {
            Logger.getLogger(CommandAttack.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
