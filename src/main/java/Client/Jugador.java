/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

import Client.FrameClient;
import Peleador.Peleador;
import Peleador.Tipo;

/**
 *
 * @author kokoju
 */
public class Jugador {
    // Atributos
    FrameClient cliente; // El jugador tiene una referencia a su cliente
    String nombre; // Nombre del jugador (usado como su ID para el ranking y los registros)
    Peleador[] peleadores = new Peleador[4]; // Arreglo que almacena los 4 peleadores del jugador
    boolean tieneComodin; // Booleano que indica si el usuario tiene un comodín disponible

    // Constructor
    public Jugador(FrameClient cliente, String nombre) {
        this.cliente = cliente;
        this.nombre = nombre;
        this.tieneComodin = false; // El jugador empieza sin comodín, entonces se pone en false
    }

    // Métodos
    public Peleador obtenerPeleador(int indiPeleador) { // Devuelve un peleador en el índice indicado. Si el índice es
                                                        // inválido, devuelve null
        if (indiPeleador >= 0 && indiPeleador < this.peleadores.length) {
            return this.peleadores[indiPeleador];
        } else
            return null;
    }

    public Peleador obtenerPrimerPeleadorActivo() {
        for (Peleador peleador : peleadores) {
            if (peleador != null && peleador.getActivo())
                return peleador;
        }
        return null;
    }

    public boolean verificarSiDerrota() { // Función para verificar si todos los peleadores del jugador fueron
                                          // derrotados
        boolean derrotado = true; // Al principio, suponemos que el jugador fue derrotado
        for (Peleador peleador : peleadores) {
            if (peleador.getActivo()) { // Si se encuentra un peleador activo
                derrotado = false; // El jugador no ha perdido
            }
        }
        return derrotado; // Se devuelve el booleano obtenido
    }

    public boolean registrarNombrePeleador(String nombrePeleador, Tipo tipo) {
        if (nombrePeleador == null || tipo == null)
            return false;

        String limpio = nombrePeleador.trim();
        if (limpio.isEmpty() || tienePeleador(limpio)) // Si esta vacio o duplicado false -> no se registro
            return false;

        for (int i = 0; i < peleadores.length; i++) {
            if (peleadores[i] == null) {
                peleadores[i] = new Peleador(limpio, tipo);
                return true; // Si no, entonces se registra y se retorna true
            }
        }
        return false;
    }

    public Peleador buscarPeleadorPorNombre(String nombrePeleador) {
        if (nombrePeleador == null)
            return null;
        String buscado = nombrePeleador.trim().toUpperCase();

        for (Peleador peleador : peleadores) {
            if (peleador == null)
                continue;
            String nombreActual = peleador.getNombre();
            if (nombreActual != null && nombreActual.trim().toUpperCase().equals(buscado))
                return peleador;
        }
        return null;
    }

    // Misma funcion que buscarPeleadorPorNombre pero retorna un booleano, mienstras
    // que la otra retorna el peleador como tal
    public boolean tienePeleador(String nombrePeleador) {
        return buscarPeleadorPorNombre(nombrePeleador) != null;
    }

    // Getters
    public FrameClient getCliente() {
        return cliente;
    }

    public String getNombre() {
        return nombre;
    }

    public Peleador[] getPeleadores() {
        return peleadores;
    }

    public boolean getTieneComodin() {
        return tieneComodin;
    }

    public int getCantidadPeleadores() {
        int count = 0;
        for (Peleador p : peleadores) {
            if (p != null)
                count++;
        }
        return count;
    }

    // Setters
    public void setTieneComodin(boolean tieneComodin) { // El comodín del jugador puede variar, entonces le creamos un
                                                        // Setter
        this.tieneComodin = tieneComodin;
    }

}
