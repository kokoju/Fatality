package Models;

import Client.Client;
import Server.ServerThread;

/**
 * Solicita al servidor las estadísticas de un jugador específico.
 */
public class CommandSelectPlayer extends Command {

    private final String targetPlayer;

    public CommandSelectPlayer(String[] args) {
        super(CommandType.SELECTPLAYER, args);
        this.consumesTurn = false;
        this.ownCommand = false;
        this.setIsBroadcast(false);
        if (args != null && args.length > 1 && args[1] != null) {
            this.targetPlayer = args[1].trim();
        } else {
            this.targetPlayer = "";
        }
    }

    @Override
    public void processForServer(ServerThread serverThread) {
        this.setIsBroadcast(false);
    }

    @Override
    public void processInClient(Client client) {
        // Este comando solo se utiliza como solicitud hacia el servidor.
    }

    public String getTargetPlayer() {
        return targetPlayer;
    }
}
