package Models;

import Cliente.Client;
import Servidor.ThreadServidor;
import Cliente.ThreadClient;

/**
 * sando
 */
public class CommandResult extends Command {

    public CommandResult(String[] args) {
        super(CommandType.RESULT, args);
        this.consumesTurn = false;
        this.ownCommand = false;
    }

    @Override
    public void processForServer(ThreadServidor threadServidor) {
        this.setIsBroadcast(true);
    }

    @Override
    public void processInClient(Client client) {
        String[] params = this.getParameters();
        String msg = "";

        if (params == null)
            return;

        // Caso especial: resultado estructurado de ataque aplicado
        if (params.length > 2 && "ATTACK_APPLIED".equals(params[2])) {
            // params: ["RESULT", <attackerName>, "ATTACK_APPLIED", <heroName>, <humanMsg>]
            msg = params[4];

            // Consumir boost localmente en el atacante (si el cliente local es el atacante)
            String attackerName = params[1];
            String heroName = params[3];
            if (attackerName.equalsIgnoreCase(client.name)) {
                if (client.getJugador() != null) {
                    Hero.Hero heroe = client.getJugador().buscarHeroe(heroName);
                    heroe.consumirStrengthen();
                }
            }
            // Registrar en la bitácora del thread local que se recibió la confirmación
            try {
                ThreadClient t = client.getThreadClient();
                if (t != null) {
                    String entrada = "ATTACK_SENT: " + msg;
                    t.addBitacora(entrada);
                }
            } catch (Exception e) {
            }
        } else if (params.length > 2) {
            msg = params[2];
            String name = params[1]; // Caso en el que el resultado va a un jugador especifico
            if (client.name == null || name == null)
                return;
            if (!client.name.trim().equalsIgnoreCase(name.trim()))
                return;
        } else
            msg = params[1];
        client.getRefFrame().writeMessage(msg);
        // Notificar posible resultado (victoria / derrota)
        try {
            Cliente.OutcomeNotifier.handleResultMessage(client, msg);
        } catch (Exception ignored) {
        }
    }
}
