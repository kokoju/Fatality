package Models;

import Client.Client;
import Server.Server;
import Server.ServerThread;

/**
 * Reporta al servidor el resumen de un ataque para actualizar las estadísticas.
 */
public class CommandUpdateSummary extends Command {

    private final String attackerName;
    private final String fighterName;
    private final String weaponName;
    private final String targetName;
    private final int totalDamage;
    private final int totalAttacks;
    private final int successfulAttacks;
    private final int failedAttacks;

    public CommandUpdateSummary(String attackerName, String fighterName, String weaponName,
            String targetName, int totalDamage,
            int totalAttacks, int successfulAttacks, int failedAttacks) {
        super(CommandType.UPDATESUMMARY, new String[] { "UPDATESUMMARY", attackerName.trim() });

        this.setIsBroadcast(false);
        this.consumesTurn = false;
        this.ownCommand = false; // Se envía al atacante

        this.attackerName = attackerName.trim();
        this.fighterName = fighterName;
        this.weaponName = weaponName;
        this.targetName = targetName;
        this.totalDamage = totalDamage;
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
        // Actualizar el panel de ataque realizado del atacante
        if (client != null && client.getRefFrame() != null) {
            client.getRefFrame().actualizarAtaqueRealizado(
                    fighterName, weaponName, targetName, totalDamage, successfulAttacks);
        }
    }

}
