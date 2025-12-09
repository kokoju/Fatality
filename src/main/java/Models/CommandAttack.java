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
 * Maneja comandos Attack <JugadorObjetivo> <Peleador> <Arma> y la variante con
 * comodín: Attack <JugadorObjetivo> Comodin <Peleador1> <Arma1> <Peleador2>
 * <Arma2>.
 */
public class CommandAttack extends Command {

    public CommandAttack(String[] args) {
        super(CommandType.ATTACK, args);
        this.consumesTurn = false;
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
        boolean comodinSolicitado = params.length >= 7 && "COMODIN".equalsIgnoreCase(params[2]);

        if (comodinSolicitado && params.length < 7) {
            cliente.getRefFrame().writeMessage(
                    "Uso: Attack <Jugador> Comodin <Peleador1> <Arma1> <Peleador2> <Arma2>");
            return;
        }

        String fighterName;
        String weaponName;
        String segundoPeleador = null;
        String segundaArma = null;

        if (comodinSolicitado) {
            fighterName = params[3].trim();
            weaponName = params[4].trim();
            segundoPeleador = params[5].trim();
            segundaArma = params[6].trim();
        } else {
            fighterName = params[2].trim();
            weaponName = params[3].trim();
        }

        if (targetName.isEmpty() || fighterName.isEmpty() || weaponName.isEmpty()) {
            cliente.getRefFrame().writeMessage("Los parámetros Jugador, Peleador y Arma son obligatorios");
            return;
        }

        if (comodinSolicitado && (segundoPeleador == null || segundaArma == null
                || segundoPeleador.trim().isEmpty() || segundaArma.trim().isEmpty())) {
            cliente.getRefFrame().writeMessage(
                    "Debe indicar ambos peleadores y armas para usar el comodín");
            return;
        }

        if (cliente.name != null && cliente.name.equalsIgnoreCase(targetName)) {
            cliente.getRefFrame().writeMessage("No puedes atacarte a ti mismo");
            return;
        }

        if (comodinSolicitado && !atacante.getTieneComodin()) {
            cliente.getRefFrame().writeMessage("No tienes un comodín disponible en este momento");
            return;
        }

        Peleador peleador = atacante.buscarPeleadorPorNombre(fighterName);
        if (peleador == null) {
            cliente.getRefFrame().writeMessage("No tienes un peleador llamado '" + fighterName + "'");
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

        Peleador segundoPeleadorObj = null;
        Arma armaSecundaria = null;

        if (comodinSolicitado) {
            segundoPeleadorObj = atacante.buscarPeleadorPorNombre(segundoPeleador);
            if (segundoPeleadorObj == null) {
                cliente.getRefFrame().writeMessage("No tienes un peleador llamado '" + segundoPeleador + "'");
                return;
            }

            armaSecundaria = segundoPeleadorObj.buscarArmaPorNombre(segundaArma);
            if (armaSecundaria == null) {
                cliente.getRefFrame().writeMessage(
                        "El peleador '" + segundoPeleador + "' no tiene un arma '" + segundaArma + "'");
                return;
            }

            if (armaSecundaria.getFueUsada()) {
                cliente.getRefFrame()
                        .writeMessage("El arma '" + segundaArma + "' ya fue usada. Selecciona otra arma.");
                return;
            }
        }

        arma.setFueUsada(true);
        if (armaSecundaria != null) {
            armaSecundaria.setFueUsada(true);
        }

        boolean comodinConsumido = false;
        if (comodinSolicitado) {
            atacante.setTieneComodin(false);
            comodinConsumido = true;
        }

        Command primerAtaque = crearComandoApply(targetName, cliente.name, fighterName, arma);
        Command segundoAtaque = null;
        if (comodinSolicitado && segundoPeleadorObj != null && armaSecundaria != null) {
            segundoAtaque = crearComandoApply(targetName, cliente.name, segundoPeleador, armaSecundaria);
        }

        try {
            cliente.objectSender.writeObject(primerAtaque);
            cliente.setUltimoEnemigoAtacado(targetName);
            cliente.getRefFrame().writeMessage(
                    "Ataque enviado a '" + targetName + "' usando '" + weaponName + "'");

            if (segundoAtaque != null) {
                cliente.objectSender.writeObject(segundoAtaque);
                cliente.getRefFrame().writeMessage(
                        "Segundo ataque del comodín enviado a '" + targetName + "' usando '" + segundaArma + "'");
            }
        } catch (IOException ex) {
            arma.setFueUsada(false);
            if (armaSecundaria != null) {
                armaSecundaria.setFueUsada(false);
            }
            if (comodinConsumido) {
                atacante.setTieneComodin(true);
            }
            Logger.getLogger(CommandAttack.class.getName()).log(Level.SEVERE, null, ex);
            cliente.getRefFrame().writeMessage("No se pudo enviar el ataque: " + ex.getMessage());
        }
    }

    private Command crearComandoApply(String targetName, String attackerName, String fighter, Arma arma) {
        String weaponName = arma != null ? arma.getNombre() : "";
        String dañosSerializados = arma != null ? serializarDaños(arma.getArregloGolpe()) : "";

        String[] args = new String[] {
                "APPLYATTACK",
                targetName,
                attackerName,
                fighter,
                weaponName,
                dañosSerializados
        };
        return new CommandApplyAttack(args);
    }

    private String serializarDaños(int[] daños) {
        if (daños == null || daños.length == 0)
            return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < daños.length; i++) {
            if (i > 0)
                sb.append(",");
            sb.append(daños[i]);
        }
        return sb.toString();
    }
}
