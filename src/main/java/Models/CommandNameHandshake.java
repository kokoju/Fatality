/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;

import Client.Client;
import Server.ServerThread;
import java.io.IOException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Comando enviado por el servidor al cliente cuando el nombre elegido no es valido o es vacio
 * @author sando
 */
public class CommandNameHandshake extends Command {

    public CommandNameHandshake(String[] args) {
        super(CommandType.NAME_HANDSHAKE, args);
        this.consumesTurn = false;
        this.ownCommand = true;
        this.setIsBroadcast(false);
    }

    @Override
    public void processForServer(ServerThread serverThread) {
        // Server never processes this command; it is only emitted toward clients.
    }

    @Override
    public void processInClient(Client client) {
        SwingUtilities.invokeLater(() -> {

            String currentAttempt = getParameters()[1];
            String reason = getParameters()[2];
            String prompt = reason + "\nIntenta con otro nombre:";
            String nuevoNombre = null;

            while (nuevoNombre == null || nuevoNombre.trim().isEmpty()) {
                nuevoNombre = JOptionPane.showInputDialog(client.getRefFrame(), prompt, currentAttempt);

                if (nuevoNombre == null) {
                    int decision = JOptionPane.showConfirmDialog(client.getRefFrame(),
                            "¿Desea salir de la aplicación?", "Confirmar salida",
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (decision == JOptionPane.YES_OPTION) {
                        System.exit(0);
                        return;
                    }
                    continue;
                }

                nuevoNombre = nuevoNombre.trim();

                if (nuevoNombre.isEmpty()) {
                    JOptionPane.showMessageDialog(client.getRefFrame(),
                            "El nombre no puede estar vacío.",
                            "Nombre requerido", JOptionPane.WARNING_MESSAGE);
                }

            }

            client.name = nuevoNombre;
            if (client.getRefFrame() != null) {
                client.getRefFrame().setTitle("Cliente de " + nuevoNombre);
            }

            try {
                client.objectSender.writeObject(CommandFactory.getCommand(new String[]{"NAME", nuevoNombre}));
            } catch (IOException ex) {
                if (client.getRefFrame() != null) {
                    client.getRefFrame().writeMessage("No se pudo reenviar el nombre: " + ex.getMessage());
                }
            }
        });
    }
}
