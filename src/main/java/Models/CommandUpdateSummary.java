package Models;

import Client.Client;
import Server.Server;
import Server.ServerThread;

/**
 * Reporta al servidor el resumen de un ataque para actualizar las estadísticas.
 */
public class CommandUpdateSummary extends Command {

    private final String attackerName;
    private final int totalAttacks;
    private final int successfulAttacks;
    private final int failedAttacks;

    public CommandUpdateSummary(String attackerName,int totalAttacks,int successfulAttacks,int failedAttacks) {
        super(CommandType.UPDATESUMMARY, new String[] { "UPDATESUMMARY", attackerName.trim() });
        
        this.setIsBroadcast(false);
        this.consumesTurn = false;
        this.ownCommand = true;
        
        this.attackerName = attackerName.trim();
        this.totalAttacks = totalAttacks;
        this.successfulAttacks = successfulAttacks;
        this.failedAttacks = failedAttacks;
    }

    @Override
    public void processForServer(ServerThread serverThread) {
        this.setIsBroadcast(false);
        
        Server server = serverThread.getServer();
        if (attackerName == null || attackerName.isEmpty())
            return;
        server.aplicarResumenAtaque(attackerName, totalAttacks, successfulAttacks, failedAttacks);
    }

    @Override
    public void processInClient(Client client) {
        // Este comando no tiene efectos en el cliente.
    }

}
