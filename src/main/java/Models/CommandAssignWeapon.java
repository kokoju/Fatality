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

/**
 * Handles AssignWeapon <Peleador> <Weapon> requests on the client side.
 */
public class CommandAssignWeapon extends Command {

    public CommandAssignWeapon(String[] args) {
        super(CommandType.ASSIGNWEAPON, args);
        this.consumesTurn = false;
        this.ownCommand = true;
        this.setIsBroadcast(false);
    }

    @Override
    public void processForServer(ServerThread serverThread) {
        // Este comando se maneja exclusivamente en el cliente.
    }

    @Override
    public void processInClient(Client client) {
        if (client == null)
            return;

        Jugador jugador = client.getJugador();
        if (jugador == null) {
            client.getRefFrame().writeMessage("Aún no tienes un jugador inicializado");
            return;
        }

        String[] params = getParameters();
        if (params == null || params.length < 3) {
            client.getRefFrame().writeMessage("Uso: AssignWeapon <Peleador> <Arma>");
            return;
        }

        String fighterName = params[1] == null ? "" : params[1].trim();
        String weaponName = params[2] == null ? "" : params[2].trim();
        if (fighterName.isEmpty()) {
            client.getRefFrame().writeMessage("El nombre del peleador no puede estar vacío");
            return;
        }
        if (weaponName.isEmpty()) {
            client.getRefFrame().writeMessage("El nombre del arma no puede estar vacío");
            return;
        }

        Peleador peleador = jugador.buscarPeleadorPorNombre(fighterName);
        if (peleador == null) {
            client.getRefFrame().writeMessage("No tienes un peleador llamado '" + fighterName + "'");
            return;
        }
        /*
         * Arma arma = GlobalArmory.buildWeapon(weaponName);
         * if (arma == null) {
         * client.getRefFrame().writeMessage("El arma '" + weaponName +
         * "' no existe en el catálogo global");
         * return;
         * }
         * boolean asignada = peleador.asignarArma(arma);
         * if (!asignada) {
         * client.getRefFrame().
         * writeMessage("No se pudo asignar el arma (ya existe o no hay espacio disponible)"
         * );
         * return;
         * }
         */

        client.getRefFrame().writeMessage(
                "Arma '" + weaponName + "' asignada a '" + fighterName + "'");
    }
}
