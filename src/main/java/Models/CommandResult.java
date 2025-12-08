package Models;

import Client.*;
import Server.ServerThread;


public class CommandResult extends Command {

    public CommandResult(String[] args) {
        super(CommandType.RESULT, args);
        this.consumesTurn = false;
        this.ownCommand = false;
    }

    @Override
    public void processForServer(ServerThread threadServidor) {
        this.setIsBroadcast(true);
    }

    @Override
    public void processInClient(Client client) {
        String[] params = this.getParameters();
        String msg = "";

        if (params == null)
            return;
        
        
        else if (params.length > 2) {
            msg = params[2];
            String name = params[1]; // Caso en el que el resultado va a un jugador especifico
            if (client.name == null || name == null)
                return;
            if (!client.name.trim().equalsIgnoreCase(name.trim()))
                return;
        } 
        
        
        else
            msg = params[1];
        client.getRefFrame().writeMessage(msg);
        // Notificar posible resultado (victoria / derrota)
        try {
            //Cliente.OutcomeNotifier.handleResultMessage(client, msg);
        } catch (Exception ignored) {
        }
    }
}
