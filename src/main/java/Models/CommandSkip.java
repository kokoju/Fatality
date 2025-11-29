/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;

import Server.ServerThread;

/**
 * Command para saltar el turno propio. Solo puede usarse en el turno del cliente que lo envía.
 */
public class CommandSkip extends Command {

    public CommandSkip(String[] args) {
        super(CommandType.SKIP, args);
        // Este comando debe ser usado únicamente en el turno propio
        this.consumesTurn = true;
        // No es un comando que se ejecute sólo en la consola emisora
        this.ownCommand = false;
    }

    @Override
    public void processForServer(ServerThread threadServidor) {
        // Transmitir como broadcast y avanzar al siguiente turno
        this.setIsBroadcast(true);
    }

    @Override
    public String toString() {
        return "Jugador saltó su turno...";
    }

}
