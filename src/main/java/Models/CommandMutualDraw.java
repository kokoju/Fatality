package Models;

import Server.Server;
import Server.ServerThread;

/**
 * Permite que los jugadores soliciten terminar la partida en empate mutuo.
 */
public class CommandMutualDraw extends Command {

    public CommandMutualDraw(String[] args) {
        super(CommandType.DRAW, args);
        this.consumesTurn = false;
        this.ownCommand = true;
        this.setIsBroadcast(false);
    }

    @Override
    public void processForServer(ServerThread serverThread) {
        this.setIsBroadcast(false);
        if (serverThread == null)
            return;

        Server server = serverThread.getServer();
        if (server == null)
            return;

        boolean esRechazo = false;
        String[] params = getParameters();
        if (params != null && params.length > 1 && params[1] != null
                && params[1].trim().equalsIgnoreCase("REJECT")) {
            esRechazo = true;
        }

        if (!server.getStart()) {
            server.enviarResultadoPrivado(serverThread, "No hay una partida en curso para finalizar.");
            return;
        }

        if (!serverThread.isActive) {
            server.enviarResultadoPrivado(serverThread, "Ya no participas en la partida.");
            return;
        }

        String jugador = serverThread.name;

        if (esRechazo) {
            server.rechazarSolicitudEmpate(jugador);
            return;
        }

        if (server.yaPropusoEmpate(jugador)) {
            server.enviarResultadoPrivado(serverThread, "Ya enviaste una propuesta de empate. Espera respuesta.");
            return;
        }

        boolean empateConfirmado = server.registrarSolicitudEmpate(jugador);
        if (empateConfirmado) {
            server.resolverEmpateMutuo();
        } else {
            server.enviarResultadoPrivado(serverThread, "Propuesta de empate enviada. Esperando confirmación.");
            server.notificarPropuestaEmpate(jugador);
        }
    }
}
