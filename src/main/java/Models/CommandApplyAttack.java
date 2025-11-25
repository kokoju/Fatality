/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;

import Cliente.Client;
import Cliente.FrameClient;
import Cliente.Jugador;
import Hero.*;
import Models.AttackPayload;
import Servidor.Server;
import Servidor.ThreadServidor;
import javax.swing.SwingUtilities;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sando
 */
public class CommandApplyAttack extends Command {

    private AttackPayload payload;


    //Contructures
    public CommandApplyAttack(String[] args) {
        super(CommandType.APPLYATTACK, args);
        this.consumesTurn = true;
        this.ownCommand = false;
    }

    public CommandApplyAttack(AttackPayload payload) {
        super(CommandType.APPLYATTACK, buildParamsFromPayload(payload));
        this.payload = payload;
        this.consumesTurn = true;
        this.ownCommand = false;


    }

    private static String[] buildParamsFromPayload(AttackPayload payload) {
        String[] extras = payload.getExtras();
        String[] params = new String[4 + (extras == null ? 0 : extras.length)];
        params[0] = "APPLYATTACK";
        params[1] = payload.getTargetName();
        params[2] = payload.getHeroType();
        params[3] = payload.getAttackType();

        if (extras != null) 
            for (int i = 0; i < extras.length; i++) 
                params[4 + i] = extras[i];
        return params;
    }



    public AttackPayload getPayload() { 
        return this.payload; 
    }

    @Override
    public void processForServer(ThreadServidor threadServidor) {
        this.setIsBroadcast(false);
    }

    
    @Override
    public void processInClient(Client clienteAtacado) { // Cliente que recibe el ataque
        // Forma esperada de parámetros:
        // [0] = "ATTACK", [1] = contrincante, [2] = Heroe, [3] = TipoAtaque, [4].. = extras

        String[] params = this.getParameters();

        // Si se recibió un payload serializado, reconstruir un arreglo de parámetros a partir de él
        if (this.payload != null) {
            String[] extras = this.payload.getExtras();
            params = new String[4 + (extras == null ? 0 : extras.length)]; //Parametos + la cantidad de extras
            params[0] = "ATTACK";
            params[1] = this.payload.getTargetName();
            params[2] = this.payload.getHeroType();
            params[3] = this.payload.getAttackType();
            if (extras != null) 
                for (int i = 0; i < extras.length; i++) 
                    params[4 + i] = extras[i];
        }

        Jugador atacado = clienteAtacado.getJugador();

        // Reconstruir un Hero atacante a partir del HeroPackage si está presente
        Hero atacanteHero = null;
        if (this.payload != null && this.payload.getHeroPackage() != null) {
            atacanteHero = HeroFactory.createFromPackage(this.payload.getHeroPackage());
            // Asignar la matriz objetivo al héroe reconstruido para que las validaciones y la ejecución
            // que dependen de `getMatrizAtaque()` funcionen correctamente en el cliente receptor.
            if (atacanteHero != null && atacado != null && atacado.getMatriz() != null) {
                atacanteHero.setMatrizAtaque(atacado.getMatriz());
                try {
                    // Attach UI parent (FrameClient) to the reconstructed hero so attacks can show popups
                    if (clienteAtacado.getRefFrame() != null) {
                        atacanteHero.setParentComponent(clienteAtacado.getRefFrame());
                    }
                } catch (Exception ignore) {}
            }
        }

        // Registrar en bitácora del receptor que se recibió un ataque
        try {
            if (clienteAtacado.getThreadClient() != null && this.payload != null) {
                String entradaRecibida = "ATTACK_RECEIVED: Ataque '" + this.payload.getAttackType() + "' del héroe '" + this.payload.getHeroType() + "' (atacante: " + this.payload.getAttackerName() + ") dirigido a " + this.payload.getTargetName();
                clienteAtacado.getThreadClient().addBitacora(entradaRecibida);
            }
        } catch (Exception e) {}

        // Comprobar y manejar reflejo mutuo en método separado
        boolean droppedByMutualReflect = false;
        try {
            droppedByMutualReflect = handleMutualReflect(clienteAtacado, atacado);
        } catch (IOException ex) {
            System.out.println("entra exepcion 1");
        }
        if (droppedByMutualReflect) 
            return;

        // Verificaciones defensivas: asegurarse de que el jugador atacado y el atacante existan
        if (atacado == null) {
            String msg = "CommandApplyAttack: jugador atacado no inicializado";
            clienteAtacado.getRefFrame().writeMessage(msg);

            if (this.payload != null && this.payload.getAttackerName() != null) {
                String[] args = new String[]{"RESULT", this.payload.getAttackerName(), msg};
                try {
                    clienteAtacado.objectSender.writeObject(Models.CommandFactory.getCommand(args));
                } catch (Exception e) {
                    // ignore send failures
                }
            }
            return;
        }

        if (atacanteHero == null) {
            String msg = "CommandApplyAttack: no se pudo reconstruir héroe atacante desde el paquete";
            clienteAtacado.getRefFrame().writeMessage(msg);
            if (this.payload != null && this.payload.getAttackerName() != null) {
                String[] args = new String[]{"RESULT", this.payload.getAttackerName(), msg};
                try {
                    clienteAtacado.objectSender.writeObject(CommandFactory.getCommand(args));
                } catch (Exception e) {
                }
            }
            // No podemos validar ni ejecutar sin un héroe atacante reconstruido
            return;
        }

        // Validar que el ataque y sus parámetros son correctos usando la lógica del héroe atacante
        boolean ataqueValido = atacanteHero.validarHeroes(params);
 

        if (!ataqueValido) {
            String msg = "CommandApplyAttack: parámetros extra incorrectos";
            if (this.payload != null && this.payload.getAttackerName() != null) {
                String[] args = new String[]{"RESULT", this.payload.getAttackerName(), msg};
                try {
                    clienteAtacado.objectSender.writeObject(CommandFactory.getCommand(args));
                } catch (Exception e) {
                    // ignore send failures
                }
            }
            return;
        }

        // Delegar la ejecución del ataque a la lógica del héroe (que crea el Ataque y llama ejecutar())
        try {
            atacanteHero.realizarAtaque(atacado, params);
        } catch (Exception ex) {
            clienteAtacado.getRefFrame().writeMessage("CommandApplyAttack: error ejecutando ataque: " + ex.getMessage());
        }

        // Actualizar UI en EDT
        SwingUtilities.invokeLater(() -> {
            clienteAtacado.getRefFrame().actualizarPnlMatriz();
            });
        String okMsg = "AttackResult:Se aplicó ataque '" + params[3] + "' de '" + params[2] + "a" + clienteAtacado.name + "'.";
        // Registrar en bitácora del receptor que el ataque se ejecutó
        try {
            if (clienteAtacado.getThreadClient() != null && this.payload != null) {
                String entrada = "ATTACK_RECEIVED: Se aplicó ataque '" + params[3] + "' de '" + params[2] + "' sobre " + clienteAtacado.name + ".";
                clienteAtacado.getThreadClient().addBitacora(entrada);
            }
        } catch (Exception e) {}

        boolean reflected = false;
        try {
            reflected = tryReflectKraken(clienteAtacado, atacado);
        } catch (IOException ex) {
            Logger.getLogger(CommandApplyAttack.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (reflected) 
            return;

        // Notificar al atacante que el ataque se aplicó
        if (this.payload != null && this.payload.getAttackerName() != null) {
            String heroName = this.payload.getHeroName();
            String[] args = new String[]{"RESULT", this.payload.getAttackerName(), "ATTACK_APPLIED", heroName, okMsg};
            try {
                clienteAtacado.objectSender.writeObject(CommandFactory.getCommand(args));
                clienteAtacado.objectSender.writeObject(CommandFactory.getCommand(new String[]{"NEXT"})); 
            } catch (Exception e) {
            }
        }
    }

    
    
    

    private boolean tryReflectKraken(Client clienteAtacado, Jugador atacado) throws java.io.IOException {
        if (this.payload == null || this.payload.getAttackType() == null) return false;
        if (!this.payload.getAttackType().equalsIgnoreCase("RELEASETHEKRAKEN")) return false;

        Hero heroeReflector = null;
        for (Hero h : atacado.getHeroes()) {
            if (h == null) continue;
            if ("PoseidonTrident".equalsIgnoreCase(h.getClass().getSimpleName())) {
                heroeReflector = h;
                break;
            }
        }

        if (heroeReflector == null) return false;
        if (this.payload.getAttackerName() == null) return false;

        // Construir HeroPackage y payload reflejado (marcado como reflejado)
        HeroPackage hp = atacado.buildHeroPackage(heroeReflector.getNombre());
        String[] extras = this.payload.getExtras();
        AttackPayload reflected = new AttackPayload(clienteAtacado.name, this.payload.getAttackerName(), heroeReflector.getNombre(), heroeReflector.getNombre(), this.payload.getAttackType(), extras, hp, true);

        if (clienteAtacado.getThreadClient() != null) {
            clienteAtacado.getThreadClient().addBitacora("ATTACK_REFLECTED: El jugador " + clienteAtacado.name + " reflejó un Kraken hacia " + this.payload.getAttackerName());
        }

        clienteAtacado.objectSender.writeObject(new CommandApplyAttack(reflected));
        return true;
    }

    // Mueve la lógica de detección/descarte de reflejo mutuo aquí para mantener processInClient limpio.
    // Retorna true si el ataque reflejado debe ser descartado (ambos pueden reflejar).
    private boolean handleMutualReflect(Client clienteAtacado, Jugador atacado) throws java.io.IOException {
        if (this.payload == null || !this.payload.isReflected()) return false;

        boolean hasPoseidon = false;
        for (Hero h : atacado.getHeroes()) {
            if (h == null) continue;
            if ("PoseidonTrident".equalsIgnoreCase(h.getClass().getSimpleName())) {
                hasPoseidon = true;
                break;
            }
        }

        if (!hasPoseidon) return false;

        // Registrar en bitácora y no aplicar
        if (clienteAtacado.getThreadClient() != null) {
            clienteAtacado.getThreadClient().addBitacora("ATTACK_DROPPED_MUTUAL_REFLECT: Se descartó un ataque reflejado de " + this.payload.getAttackerName() + " porque el receptor también puede reflejar.");
        }

        // Informar al atacante original que su ataque fue neutralizado (opcional)
        if (this.payload.getAttackerName() != null) {
            clienteAtacado.objectSender.writeObject(CommandFactory.getCommand(new String[]{"RESULT", this.payload.getAttackerName(), "ATTACK_NEUTRALIZED_BY_MUTUAL_REFLECT"}));
        }

        return true;
    }

}
