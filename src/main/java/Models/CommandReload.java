package Models;

import Client.Arma;
import Client.Client;
import Client.FrameClient;
import Client.Jugador;
import Peleador.Peleador;
import Server.ServerThread;

/**
 * Comando local para recargar todas las armas del jugador.
 */
public class CommandReload extends Command {

    public CommandReload(String[] args) {
        super(CommandType.RELOAD, args);
        this.consumesTurn = true;
        this.ownCommand = true;
        this.setIsBroadcast(false);
    }

    @Override
    public void processForServer(ServerThread serverThread) {
        this.setIsBroadcast(false);
    }

    @Override
    public void processInClient(Client client) {
        if (client == null)
            return;

        Jugador jugador = client.getJugador();
        FrameClient frame = client.getRefFrame();
        if (jugador == null) {
            if (frame != null)
                frame.writeMessage("No se puede recargar: aún no tienes un jugador inicializado.");
            return;
        }

        Peleador[] peleadores = jugador.getPeleadores();
        if (peleadores == null || peleadores.length == 0) {
            if (frame != null)
                frame.writeMessage("No tienes peleadores registrados para recargar armas.");
            return;
        }

        int armasRecargadas = 0;
        for (Peleador peleador : peleadores) {
            if (peleador == null)
                continue;
            Arma[] armas = peleador.getArregloArmas();
            if (armas == null)
                continue;
            for (Arma arma : armas) {
                if (arma == null)
                    continue;
                arma.generarNuevoArregloGolpe();
                arma.setFueUsada(false);
                armasRecargadas++;
            }
        }

        if (frame != null) {
            if (armasRecargadas == 0) {
                frame.writeMessage("No se encontraron armas para recargar.");
            } else {
                frame.writeMessage("Armas recargadas: " + armasRecargadas + ". Tu turno ha finalizado.");
            }
        }
    }
}
