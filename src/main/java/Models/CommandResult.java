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

        String mensajeNormalizado = msg == null ? "" : msg.trim();
        if (mensajeNormalizado.equalsIgnoreCase("GANASTE")) {
            mostrarDialogoFinPartida(client, "¡Has ganado la partida!", "Victoria");
        } else if (mensajeNormalizado.equalsIgnoreCase("La partida terminó en empate mutuo.")) {
            mostrarDialogoFinPartida(client, mensajeNormalizado, "Empate");
        }
    }

    //Si es el ultimo con vida o se acuerda un empate mutuo, se muestra un dialogo y se cierra el cliente y el programa
    private void mostrarDialogoFinPartida(Client client, String mensaje, String titulo) {
        if (client == null || client.getRefFrame() == null)
            return;
        javax.swing.SwingUtilities.invokeLater(() -> {
            javax.swing.JOptionPane.showMessageDialog(
                    client.getRefFrame(),
                    mensaje,
                    titulo,
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
            try {
                if (client.objectSender != null)
                    client.objectSender.close();
                if (client.objectListener != null)
                    client.objectListener.close();
            } catch (Exception ignored) {
            }
            javax.swing.JFrame frame = client.getRefFrame();
            if (frame != null) {
                frame.dispose();
            }
            System.exit(0);
        });
    }
}
