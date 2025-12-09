package Models;

import Client.Client;
import Server.ServerThread;

/**
 * Comando para enviar estadísticas actualizadas a un cliente.
 * Incluye ranking, stats propias y stats del último enemigo atacado.
 */
public class CommandUpdateStats extends Command {

    private final String ranking;
    private final String ownStats;
    private final String enemyStats;

    public CommandUpdateStats(String ranking, String ownStats, String enemyStats) {
        super(CommandType.UPDATESTATS, new String[] { "UPDATESTATS" });

        this.setIsBroadcast(false);
        this.consumesTurn = false;
        this.ownCommand = false;

        if (ranking != null) {
            this.ranking = ranking;
        } else {
            this.ranking = "";
        }

        if (ownStats != null) {
            this.ownStats = ownStats;
        } else {
            this.ownStats = "";
        }

        if (enemyStats != null) {
            this.enemyStats = enemyStats;
        } else {
            this.enemyStats = "";
        }
    }

    @Override
    public void processForServer(ServerThread serverThread) {
        this.setIsBroadcast(false);
    }

    @Override
    public void processInClient(Client client) {
        if (client == null || client.getRefFrame() == null)
            return;

        // Siempre actualizar ranking y stats propias
        client.getRefFrame().setTextTxaRanking(ranking);
        client.getRefFrame().setTextTxaStats(ownStats);

        // Solo actualizar panel enemigo si hay datos (no borrar el existente)
        if (!enemyStats.isEmpty()) {
            client.getRefFrame().setTextTxaEnemyStats(enemyStats);
        }
    }

    public String getRanking() {
        return ranking;
    }

    public String getOwnStats() {
        return ownStats;
    }

    public String getEnemyStats() {
        return enemyStats;
    }
}
