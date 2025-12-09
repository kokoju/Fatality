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
        this.ownCommand = false; // Se envía al servidor para validar turno primero
        this.setIsBroadcast(false);
    }

    @Override
    public void processForServer(ServerThread serverThread) {
        // El servidor valida el turno y reenvía al atacante para que lo procese
        this.setIsBroadcast(false);
        this.ownCommand = true; // Ahora se procesa en el cliente del atacante
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

        // Verificar que el peleador esté vivo
        if (!peleador.getActivo() || peleador.getVida() <= 0) {
            cliente.getRefFrame().writeMessage("El peleador '" + fighterName + "' está muerto y no puede atacar");
            return;
        }

        Arma arma = peleador.buscarArmaPorNombre(weaponName);
        if (arma == null) {
            cliente.getRefFrame()
                    .writeMessage("El peleador '" + fighterName + "' no tiene un arma '" + weaponName + "'");
            return;
        }

        if (arma.getFueUsada()) {
            cliente.getRefFrame().writeMessage("El arma '" + weaponName + "' ya fue usada. Selecciona otra arma.");
            return;
        }

        arma.setFueUsada(true);

        // Convertir array de daños a string para enviarlo
        int[] daños = arma.getArregloGolpe();
        StringBuilder dañosStr = new StringBuilder();
        if (daños != null) {
            for (int i = 0; i < daños.length; i++) {
                if (i > 0)
                    dañosStr.append(",");
                dañosStr.append(daños[i]);
            }
        }

        String[] newArgs = new String[] {
                "APPLYATTACK",
                targetName, // Jugador objetivo
                cliente.name, // Nombre del atacante
                fighterName, // Nombre del peleador
                weaponName, // Nombre del arma
                dañosStr.toString() // Daños del arma (formato: "d0,d1,d2,...")
        };
        Command applyAttack = new CommandApplyAttack(newArgs);

        try {
            cliente.objectSender.writeObject(applyAttack);
            cliente.setUltimoEnemigoAtacado(targetName); // Guardar último enemigo atacado
            cliente.getRefFrame().writeMessage("Ataque enviado a '" + targetName + "' usando '" + weaponName + "'");
            // El panel de ataque realizado se actualizará cuando llegue la confirmación del
            // servidor
        } catch (IOException ex) {
            // Si falla el envío, revertir el uso del arma
            arma.setFueUsada(false);
            Logger.getLogger(CommandAttack.class.getName()).log(Level.SEVERE, null, ex);
            cliente.getRefFrame().writeMessage("No se pudo enviar el ataque: " + ex.getMessage());
        }
    }

}
