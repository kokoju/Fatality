/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;

import Client.Client;
import Client.FrameClient;
import Client.Jugador;
import Peleador.Peleador;
import Server.ServerThread;
import java.io.IOException;

/**
 * Comando que se envía al cliente objetivo para aplicar el daño.
 */
public class CommandApplyAttack extends Command {

    private static final int MIN_SUCCESS_DAMAGE = 60;

    private String attackerFighterName;
    private String attackerName;
    private String weaponName;
    private int[] weaponDamages;

    public CommandApplyAttack(String[] args) {
        super(CommandType.APPLYATTACK, args); // 0 -> APPLYATTACK , 1 -> targetName
        if (args.length > 2) {
            this.attackerName = args[2];
        } else {
            this.attackerName = "";
        }

        if (args.length > 3) {
            this.attackerFighterName = args[3];
        } else {
            this.attackerFighterName = "";
        }

        if (args.length > 4) {
            this.weaponName = args[4];
        } else {
            this.weaponName = "";
        }

        // Parsear los daños del arma (formato: "d0,d1,d2,...")
        if (args.length > 5 && args[5] != null && !args[5].isEmpty()) {
            String[] partes = args[5].split(",");
            this.weaponDamages = new int[partes.length];
            for (int i = 0; i < partes.length; i++) {
                try {
                    this.weaponDamages[i] = Integer.parseInt(partes[i].trim());
                } catch (NumberFormatException e) {
                    this.weaponDamages[i] = 0;
                }
            }
        } else {
            this.weaponDamages = new int[0];
        }

        this.consumesTurn = false; // No consume turno, es parte del flujo del ataque
        this.ownCommand = false;
        this.setIsBroadcast(false);
    }

    @Override
    public void processForServer(ServerThread serverThread) {
        this.setIsBroadcast(false);
    }

    @Override
    public void processInClient(Client clienteAtacado) {
        Jugador jugador = clienteAtacado.getJugador();
        Peleador[] peleadores = jugador.getPeleadores();

        int succesfullattacks = 0;
        int failedattacks = 0;
        int totalAttacks = 0;
        int[] dañosPorLuchador = new int[peleadores.length];

        for (int i = 0; i < peleadores.length; i++) {
            Peleador objetivo = peleadores[i];
            if (objetivo == null) {
                dañosPorLuchador[i] = 0;
                continue;
            }

            // Calcular daño basado en el tipo del peleador objetivo
            int dano = calcularDañoContraPeleador(objetivo);
            dañosPorLuchador[i] = dano;
            totalAttacks++;

            if (dano >= MIN_SUCCESS_DAMAGE) {
                objetivo.recibirGolpe(dano);
                succesfullattacks++;
            } else {
                failedattacks++;
            }
        }

        // Calcular daño total
        int dañoTotal = 0;
        for (int d : dañosPorLuchador) {
            if (d >= MIN_SUCCESS_DAMAGE) {
                dañoTotal += d;
            }
        }

        // Actualizar panel de ataque recibido con info completa
        clienteAtacado.getRefFrame().actualizarAtaqueRecibido(
                attackerName,
                attackerFighterName,
                weaponName,
                dañosPorLuchador);

        enviarResumen(clienteAtacado, dañoTotal, totalAttacks, succesfullattacks, failedattacks);
    }

    /**
     * Calcula el daño que el arma causa a un peleador según su tipo.
     * Usa el ordinal del Tipo para indexar el arregloGolpe del arma.
     */
    private int calcularDañoContraPeleador(Peleador objetivo) {
        if (objetivo == null || weaponDamages == null || weaponDamages.length == 0)
            return 0;
        int index = objetivo.getTipo().ordinal();
        if (index < 0 || index >= weaponDamages.length)
            return 0;
        return weaponDamages[index];
    }

    private void enviarResumen(Client clienteAtacado,
            int dañoTotal,
            int totalAttacks,
            int successfulAttacks,
            int failedAttacks) {
        if (clienteAtacado == null || clienteAtacado.objectSender == null)
            return;
        if (attackerName == null || attackerName.trim().isEmpty())
            return;

        // Obtener el nombre del jugador atacado
        String targetName = "";
        if (clienteAtacado.name != null) {
            targetName = clienteAtacado.name;
        }

        CommandUpdateSummary summary = new CommandUpdateSummary(
                attackerName,
                attackerFighterName,
                weaponName,
                targetName,
                dañoTotal,
                totalAttacks,
                successfulAttacks,
                failedAttacks);
        try {
            clienteAtacado.objectSender.writeObject(summary);
        } catch (IOException ex) {
            FrameClient frame = clienteAtacado.getRefFrame();
            if (frame != null)
                frame.writeMessage("No se pudo enviar el resumen del ataque: " + ex.getMessage());
        }
    }

}
