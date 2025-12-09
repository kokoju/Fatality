/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;

import Client.Client;
import Client.Jugador;
import Peleador.Tipo;
import Server.ServerThread;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * author sando
 */
public class CommandCreateFighter extends Command {

    public CommandCreateFighter(String[] args) {
        super(CommandType.CREATEFIGHTER, args);
        this.consumesTurn = false;
        this.ownCommand = true;
        this.setIsBroadcast(false);
    }

    @Override
    public void processForServer(ServerThread serverThread) {
        // Este comando se maneja exclusivamente en el cliente.
    }

    @Override
    public void processInClient(Client client) {
        if (client == null)
            return;
        Jugador jugador = client.getJugador();
        if (jugador == null) {
            client.getRefFrame().writeMessage("Aún no tienes un jugador inicializado");
            return;
        }

        String[] params = getParameters();
        if (params == null || params.length < 3) {
            client.getRefFrame().writeMessage("Uso: CreateFighter <Nombre> <Tipo>");
            return;
        }

        String fighterName = params[1] == null ? "" : params[1].trim();
        if (fighterName.isEmpty()) {
            client.getRefFrame().writeMessage("El nombre del peleador no puede estar vacío");
            return;
        }

        // Validar contra la lista oficial de peleadores permitidos
        String[] permitidos = new String[] {
                "black_manta",
                "hellboy",
                "omni_man",
                "peacemaker",
                "red_hood",
                "robocop",
                "scarecrow",
                "scorpion",
                "spawn",
                "sub_zero"
        };
        String nombreNormalizado = fighterName.trim().toLowerCase();
        boolean esPermitido = java.util.Arrays.stream(permitidos).anyMatch(p -> p.equals(nombreNormalizado));
        if (!esPermitido) {
            client.getRefFrame().writeMessage("Nombre inválido. Permitidos: " + formatearListaPermitidos(permitidos));
            return;
        }

        String tipoRaw = params[2] == null ? "" : params[2].trim();
        Tipo tipo;
        try {
            tipo = Tipo.valueOf(tipoRaw.toUpperCase());
        } catch (IllegalArgumentException ex) {
            client.getRefFrame().writeMessage("Tipo inválido. Valores permitidos: " + availableTypes());
            return;
        }

        // Verificar límite de 4 peleadores antes de crear
        if (jugador.getCantidadPeleadores() >= 4) {
            client.getRefFrame().writeMessage("No puedes crear más de 4 luchadores por cliente");
            return;
        }

        if (jugador.tienePeleador(fighterName)) {
            client.getRefFrame().writeMessage("Ya tienes un peleador llamado '" + fighterName + "'");
            return;
        }

        boolean registrado = jugador.registrarNombrePeleador(fighterName, tipo);
        if (!registrado) {
            client.getRefFrame().writeMessage("No se pudo registrar el peleador (lista llena o duplicado)");
            return;
        }

        // Actualizar imagen en el pnlMenu del FrameClient
        try {
            client.getRefFrame().establecerImagenLuchador(fighterName);
        } catch (Exception e) {
            client.getRefFrame().writeMessage("No se pudo cargar la imagen para '" + fighterName + "'");
        }

        client.getRefFrame().writeMessage("Peleador '" + fighterName + "' creado como " + tipo.toString()
            + ". Recuerda usar AssignWeapon para equiparlo.");
    }

    private String availableTypes() {
        return Arrays.stream(Tipo.values())
                .map(Tipo::name)
                .collect(Collectors.joining(", "));
    }

    /**
     * Genera una lista legible de nombres de luchadores permitidos para mostrar al
     * usuario.
     */
    private String formatearListaPermitidos(String[] permitidos) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < permitidos.length; i++) {
            sb.append(formatearNombreLuchador(permitidos[i]));
            if (i < permitidos.length - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    /**
     * Formatea el nombre de un luchador eliminando guiones bajos y capitalizando
     * cada palabra.
     * Ejemplo: "black_manta" -> "Black Manta"
     */
    private String formatearNombreLuchador(String nombre) {
        if (nombre == null || nombre.isEmpty())
            return "";
        String[] partes = nombre.split("_");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < partes.length; i++) {
            if (partes[i].length() > 0) {
                sb.append(Character.toUpperCase(partes[i].charAt(0)));
                if (partes[i].length() > 1) {
                    sb.append(partes[i].substring(1).toLowerCase());
                }
            }
            if (i < partes.length - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

}
