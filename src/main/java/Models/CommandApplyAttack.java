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

    private final String attackerFighterName;
    private final String attackerName;
    private final String weaponName;
    private final String targetName;
    private final int[] weaponDamages;

    public CommandApplyAttack(String[] args) {
        super(CommandType.APPLYATTACK, args); // 0 -> APPLYATTACK , 1 -> targetName
        this.targetName = args.length > 1 ? args[1] : "";
        this.attackerName = args.length > 2 ? args[2] : "";
        this.attackerFighterName = args.length > 3 ? args[3] : "";
        this.weaponName = args.length > 4 ? args[4] : "";

        if (args.length > 5 && args[5] != null && !args[5].trim().isEmpty()) {
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

        this.consumesTurn = true;
        this.ownCommand = false;
        this.setIsBroadcast(false);
    }

    @Override
    public void processForServer(ServerThread serverThread) {
        this.setIsBroadcast(false);
    }

    @Override
    public void processInClient(Client clienteAtacado) {

        if (clienteAtacado == null)
            return;
        Jugador jugador = clienteAtacado.getJugador();
        if (jugador == null)
            return;

        Peleador[] peleadores = jugador.getPeleadores();
        if (peleadores == null)
            return;

        int[] dañosPorLuchador = new int[peleadores.length];
        int succesfullattacks = 0;
        int failedattacks = 0;
        int totalAttacks = 0;
        int dañoTotal = 0;

        for (int i = 0; i < peleadores.length; i++) {
            Peleador objetivo = peleadores[i];
            if (objetivo == null) {
                dañosPorLuchador[i] = 0;
                continue;
            }

            int dano = calcularDañoContraPeleador(objetivo);
            dañosPorLuchador[i] = dano;
            totalAttacks++;

            if (dano >= MIN_SUCCESS_DAMAGE) {
                objetivo.recibirGolpe(dano);
                succesfullattacks++;
                dañoTotal += dano;
            } else {
                failedattacks++;
            }
        }

        if (clienteAtacado.getRefFrame() != null) {
            clienteAtacado.getRefFrame().actualizarAtaqueRecibido(
                    attackerName,
                    attackerFighterName,
                    weaponName,
                    dañosPorLuchador);
        }

        enviarResumen(clienteAtacado, dañoTotal, totalAttacks, succesfullattacks, failedattacks);
    }

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

        String target = clienteAtacado.name != null ? clienteAtacado.name : targetName;

        CommandUpdateSummary summary = new CommandUpdateSummary(
                attackerName,
                attackerFighterName,
                weaponName,
                target,
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
