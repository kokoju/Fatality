package Models;

import Client.Client;
import Client.FrameClient;
import Client.Jugador;
import Server.ServerThread;

/**
 * Notifica a un jugador si tiene disponible el comodín.
 */
public class CommandComodinStatus extends Command {

    private final String targetName;
    private final boolean disponible;
    private final String mensaje;

    public CommandComodinStatus(String[] args) {
        super(CommandType.COMODINSTATUS, args);
        this.consumesTurn = false;
        this.ownCommand = false;
        this.setIsBroadcast(false);

        if (args.length > 1) {
            this.targetName = args[1];
        } else {
            this.targetName = "";
        }

        if (args.length > 2) {
            this.disponible = Boolean.parseBoolean(args[2].trim());
        } else {
            this.disponible = false;
        }

        if (args.length > 3) {
            this.mensaje = args[3];
        } else {
            this.mensaje = "";
        }
    }

    // Constructor auxiliar que arma los argumentos a partir de datos tipados.
    public CommandComodinStatus(String jugador, boolean disponible, String mensaje) {
        this(buildArgs(jugador, disponible, mensaje));
    }

    // Normaliza los valores enviados al constructor principal del comando.
    private static String[] buildArgs(String jugador, boolean disponible, String mensaje) {
        return new String[] {
                "COMODINSTATUS",
                jugador == null ? "" : jugador,
                Boolean.toString(disponible),
                mensaje == null ? "" : mensaje
        };
    }

    @Override
    public void processForServer(ServerThread serverThread) {
        this.setIsBroadcast(false);
    }

    @Override
    public void processInClient(Client client) {
        if (client == null || client.name == null)
            return;
        if (targetName == null || targetName.trim().isEmpty())
            return;
        if (!client.name.equalsIgnoreCase(targetName.trim()))
            return;

        Jugador jugador = client.getJugador();
        if (jugador != null) {
            jugador.setTieneComodin(disponible);
        }

        FrameClient frame = client.getRefFrame();
        if (frame != null) {
            String msg = mensaje;
            if (msg == null || msg.trim().isEmpty()) {
                msg = disponible
                        ? "¡Has recibido el comodín! Usa 'Attack <Jugador> Comodin <Peleador1> <Arma1> <Peleador2> <Arma2>'."
                        : "Tu comodín ya no está disponible.";
            }
            frame.writeMessage(msg);
        }
    }
}
