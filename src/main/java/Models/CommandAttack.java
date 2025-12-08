/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;

import Client.Arma;
import Client.Client;
import Client.Jugador;
import Peleador.Peleador;
import Server.ServerThread;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Maneja comandos Attack <JugadorObjetivo> <PeleadorPropio> <Arma>.
 */
public class CommandAttack extends Command {

    public CommandAttack(String[] args) {
        super(CommandType.ATTACK, args);
        this.consumesTurn = true;
        this.ownCommand = true; // Se procesa localmente y desde aquí se orquesta el ApplyAttack
        this.setIsBroadcast(false);
    }

    @Override
    public void processForServer(ServerThread serverThread) {
        this.setIsBroadcast(false);
    }

    @Override
    public void processInClient(Client cliente) {
        Jugador atacante = cliente.getJugador();
        if (atacante == null) {
            cliente.getRefFrame().writeMessage("Imposible atacar: tu jugador aún no está inicializado");
            return;
        }

        // Caso cantidad de parámetros insuficiente
        String[] params = getParameters();
        if (params == null || params.length < 4) {
            cliente.getRefFrame().writeMessage("Uso: Attack <Jugador> <Peleador> <Arma>");
            return;
        }

        String targetName = params[1].trim();
        String fighterName = params[2].trim();
        String weaponName = params[3].trim();

        if (targetName.isEmpty() || fighterName.isEmpty() || weaponName.isEmpty()) {
            cliente.getRefFrame().writeMessage("Los parámetros Jugador, Peleador y Arma son obligatorios");
            return;
        }

        if (cliente.name != null && cliente.name.equalsIgnoreCase(targetName)) {
            cliente.getRefFrame().writeMessage("No puedes atacarte a ti mismo");
            return;
        }

        Peleador peleador = atacante.buscarPeleadorPorNombre(fighterName);
        if (peleador == null) {
            cliente.getRefFrame().writeMessage("No tienes un peleador llamado '" + fighterName + "'");
            return;
        }

        Arma arma = peleador.buscarArmaPorNombre(weaponName);
        if (arma == null) {
            cliente.getRefFrame().writeMessage("El peleador '" + fighterName + "' no tiene un arma '" + weaponName + "'");
            return;
        }

        if(arma.getFueUsada()){
            cliente.getRefFrame().writeMessage("El arma '" + weaponName + "' ya fue usada. Selecciona otra arma.");
            return;
        }

        arma.setFueUsada(true);

        String[] newArgs = new String[] {
            "APPLYATTACK",
            targetName,   // Jugador objetivo
            cliente.name, // Nombre del atacante
        };
        Command applyAttack = new CommandApplyAttack(newArgs);
        
        try {
            cliente.objectSender.writeObject(applyAttack);
            cliente.getRefFrame().writeMessage("Ataque enviado a '" + targetName + "' usando '" + weaponName + "'");
        } catch (IOException ex) {
            Logger.getLogger(CommandAttack.class.getName()).log(Level.SEVERE, null, ex);
            cliente.getRefFrame().writeMessage("No se pudo enviar el ataque: " + ex.getMessage());
        }
    }



}
