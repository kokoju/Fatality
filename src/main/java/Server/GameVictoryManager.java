package Server;

import Models.CommandFactory;
import Models.Command;
import java.io.IOException;

/**
 * Gestiona la condición de victoria del juego.
 * Regla: Cuando solo queda un jugador activo, ese jugador gana y todos los
 * demás pierden.
 * Se envían mensajes RESULT individuales: {"RESULT", <playerName>, "GANASTE"} o
 * "PERDISTE".
 */
public final class GameVictoryManager {

    private GameVictoryManager() {
    }

    // Comprueba si el juego tiene un único jugador activo y declara ganador.
    public static void checkVictory(Server server) {
        if (server == null)
            return;
        if (!server.getStart())
            return; // Juego no iniciado todavía

        int activos = 0;
        ServerThread posibleGanador = null;
        for (ServerThread ts : server.getConnectedClients()) {
            if (ts.isActive) {
                activos++;
                posibleGanador = ts;
                if (activos > 1) { // Más de uno activo, nada que hacer
                    return;
                }
            }
        }

        // Si no hay activos, partida terminada sin ganador claro: ignorar.
        if (activos == 0)
            return;

        // Activos == 1 -> declarar ganador
        if (posibleGanador != null) {
            for (ServerThread ts : server.getConnectedClients()) {
                try {
                    String msg;
                    if (ts == posibleGanador) {
                        msg = "GANASTE";
                    } else {
                        msg = "PERDISTE";
                    }
                    String[] args = new String[] { "RESULT", ts.name, msg };
                    Command comando = CommandFactory.getCommand(args);
                    ts.objectSender.writeObject(comando);
                } catch (IOException ignored) {
                }
            }
        }
    }
}

