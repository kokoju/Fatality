/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

import Client.FrameClient;
import Peleador.Peleador;

/**
 *
 * @author kokoju
 */
public class Jugador {
    // Atributos
    FrameClient cliente;  // El jugador tiene una referencia a su cliente
    String nombre;  // Nombre del jugador (usado como su ID para el ranking y los registros)
    Peleador[] peleadores;  // Arreglo que almacena los 4 peleadores del jugador
    boolean tieneComodin;  // Booleano que indica si el usuario tiene un comodín disponible
    
    // Constructor
    public Jugador(FrameClient cliente, String nombre, Peleador[] peleadores) {
        this.cliente = cliente;
        this.nombre = nombre;
        this.peleadores = peleadores;
        this.tieneComodin = false;  // El jugador empieza sin comodín, entonces se pone en false
    }
    
    // Métodos
    public Peleador obtenerPeleador(int indiPeleador) {  // Devuelve un peleador en el índice indicado. Si el índice es inválido, devuelve null
        if (indiPeleador >= 0 && indiPeleador < this.peleadores.length) {
            return this.peleadores[indiPeleador];
        }
        else return null;
    }
    
    public boolean verificarSiDerrota() {  // Función para verificar si todos los peleadores del jugador fueron derrotados
        boolean derrotado = true;  // Al principio, suponemos que el jugador fue derrotado
        for (Peleador peleador : peleadores) {
            if (peleador.getActivo()) {  // Si se encuentra un peleador activo
                derrotado = false;  // El jugador no ha perdido
            }
        }
        return derrotado;  // Se devuelve el booleano obtenido
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
    
    // Setters
    public void setTieneComodin(boolean tieneComodin) {  // El comodín del jugador puede variar, entonces le creamos un Setter
        this.tieneComodin = tieneComodin;
    }
    
}
