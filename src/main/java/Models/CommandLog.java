package Models;

import Cliente.Client;
import Servidor.ThreadServidor;
import java.util.ArrayList;

/**
 *
 * @author sando
 */
public class CommandLog extends Command {

    public CommandLog(String[] args) {
        super(CommandType.LOG, args);
        this.consumesTurn = false;
        this.ownCommand = true;
    }

    @Override
    public void processForServer(ThreadServidor threadServidor) {
        this.setIsBroadcast(false);
    }

    @Override
    public void processInClient(Client client) {
        if (client == null) return;
        if (client.getRefFrame() == null) return;

        // Obtain the ThreadClient and its bitacora
        try {
            ArrayList<String> bit = client.getThreadClient().getBitacora();
            if (bit == null || bit.isEmpty()) {
                client.getRefFrame().writeMessage("[BITACORA] Sin eventos registrados");
                return;
            }

            client.getRefFrame().writeMessage("[BITACORA] Inicio de registros:");
            for (String evento : bit) {
                client.getRefFrame().writeMessage(" - " + evento);
            }
            client.getRefFrame().writeMessage("[BITACORA] Fin de registros.");
        } catch (Exception ex) {
            try { client.getRefFrame().writeMessage("[BITACORA] Error mostrando bitácora: " + ex.getMessage()); } catch (Exception e) {}
        }
    }
}
