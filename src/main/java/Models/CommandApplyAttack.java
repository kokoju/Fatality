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
        super(CommandType.APPLYATTACK, args);   // 0 -> APPLYATTACK , 1 -> targetName
        this.attackerName = args.length > 2 ? args[2] : "";
        this.attackerFighterName = args.length > 3 ? args[3] : "";
        this.weaponName = args.length > 4 ? args[4] : "";
        

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

        Jugador jugador = clienteAtacado.getJugador();
        Peleador[] peleadores = jugador.getPeleadores();
        //weaponDamages = ensureWeaponDamages();  //TODO ARMORY DE TODAS LAS ARMAS


        int succesfullattacks = 0;
        int failedattacks = 0;
        int totalAttacks = 0;

        for (Peleador objetivo : peleadores) {
            if (objetivo == null)
                continue;

            int dano = 0; // TODO: calcular el daño real usando la tabla del arma
            //int dano = damageAgainst(objetivo);
            totalAttacks++;
            if(dano >= MIN_SUCCESS_DAMAGE) {
                objetivo.recibirGolpe(dano);
                succesfullattacks++;
            } else {
                failedattacks++;
            }
        }

        enviarResumen(clienteAtacado, totalAttacks, succesfullattacks, failedattacks);
    }

    /* TODO logica para obtener dano de un arma respecto al tipo de un peleador
    private int damageAgainst(Peleador objetivo) {
        if (objetivo == null || weaponDamages.length == 0)
            return 0;
        int index = objetivo.getTipo().ordinal();
        if (index < 0 || index >= weaponDamages.length)
            return 0;
        return weaponDamages[index];
    }
    */
    /*    TODO logica para tener las armas creadas, asi obtener sus daños respecto a tipos de peleadores
    private int[] ensureWeaponDamages() {
        if (weaponDamages != null && weaponDamages.length > 0)
            return weaponDamages;
        weaponDamages = GlobalArmory.getWeaponDamages(weaponName);
        return weaponDamages == null ? new int[0] : weaponDamages;
    }
    */

    private void enviarResumen(Client clienteAtacado,
                               int totalAttacks,
                               int successfulAttacks,
                               int failedAttacks) {
        if (clienteAtacado == null || clienteAtacado.objectSender == null)
            return;
        if (attackerName == null || attackerName.trim().isEmpty())
            return;
        CommandUpdateSummary summary = new CommandUpdateSummary(
                attackerName,
                totalAttacks,
                successfulAttacks,
                failedAttacks
        );
        try {
            clienteAtacado.objectSender.writeObject(summary);
        } catch (IOException ex) {
            FrameClient frame = clienteAtacado.getRefFrame();
            if (frame != null)
                frame.writeMessage("No se pudo enviar el resumen del ataque: " + ex.getMessage());
        }
    }
    
}
