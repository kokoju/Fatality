package Server;

import Models.CommandFactory;
import Models.Command;
import java.io.IOException;

/**
 * Gestiona la condición de victoria del juego.
 * Un jugador está vivo si al menos uno de sus luchadores tiene vida > 0.
 * Cuando solo queda un jugador vivo, ese jugador gana y los demás pierden.
 */
public final class GameVictoryManager {

    private GameVictoryManager() {
    }

    /**
     * Verifica el estado de victoria del juego.
     * Si solo queda un jugador activo, declara la victoria.
     */
    public static void checkVictory(Server server) {
        if (server == null)
            return;
        if (!server.getStart())
            return;

        // Contar jugadores activos
        int activos = 0;
        ServerThread posibleGanador = null;
        for (ServerThread ts : server.getConnectedClients()) {
            if (ts.isActive) {
                activos++;
                posibleGanador = ts;
                if (activos > 1) {
                    return;
                }
            }
        }

        if (activos == 0)
            return;

        // Activos == 1 -> declarar ganador
        if (posibleGanador != null) {
            declararGanador(server, posibleGanador);
        }
    }

    /**
     * Declara al ganador y envía mensajes a todos los jugadores.
     */
    private static void declararGanador(Server server, ServerThread ganador) {
        // Actualizar estadísticas
        server.incrementarStatJugador(ganador.name, "WINS");

        for (ServerThread ts : server.getConnectedClients()) {
            if (ts.name == null) {
                continue;
            }

            try {
                String msg;
                if (ts == ganador) {
                    msg = "¡GANASTE LA PARTIDA!";
                } else {
                    msg = "PERDISTE. Ganador: " + ganador.name;
                    server.incrementarStatJugador(ts.name, "LOSSES");
                }
                String[] args = new String[] { "RESULT", ts.name, msg };
                Command comando = CommandFactory.getCommand(args);
                ts.objectSender.writeObject(comando);
            } catch (IOException ignored) {
            }
        }

        // Mostrar mensaje en el servidor
        server.getRefFrame().writeMessage("=== PARTIDA TERMINADA ===");
        server.getRefFrame().writeMessage("Ganador: " + ganador.name);
    }
}
