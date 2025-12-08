/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Peleador;

import Client.Arma;

/**
 *
 * @author kokoju
 */
public class Peleador {
    private static final int MAX_ARMAS = 5;  // Capacidad maxima de armas
    // Atributos
    String name;  // Nombre del peleador
    int vida;  // Vida del peleador
    Arma[] arregloArmas = new Arma[MAX_ARMAS];  // Arreglo de las armas del peleador
    Tipo tipo;  // Tipo al que pertenece el peleador
    boolean activo;  // Booleano para verificar si el peleador está activo
    
    // Constructor
    public Peleador(String name, Tipo tipo) {  // Como estas caracteristicas las elige el jugador, debemos hacerlo de esta menra
        this.name = name;
        this.vida = 100;
        this.tipo = tipo;
        this.activo = true;
    }
    
    // Métodos
    public void recibirGolpe(int golpe) {
        this.vida -= golpe;  // Se le resta el golpe a la vida del personaje
        if (this.vida <= 0) {  // Si la vida del personaje llega a 0 o menos
            this.vida = 0;  // Se establece la vida en 0 para no mostrar negativos
            this.activo = false;
        }
    }
    
    public void recargarArma(int indiArma) {
        if(this.arregloArmas == null) {
            System.out.println("Error: El peleador no tiene armas asignadas");
            return;
        }
        this.arregloArmas[indiArma].generarNuevoArregloGolpe();  // Para la recarga, se genera un nuevo arreglo de golpe
    }

    public boolean asignarArma(Arma arma) {
        if (arma == null)
            return false;

        if (this.arregloArmas == null) {
            this.arregloArmas = new Arma[MAX_ARMAS];
        }

        for (Arma existente : this.arregloArmas) {
            if (existente == null)
                continue;
            String nombreExistente = existente.getNombre();
            if (nombreExistente != null && nombreExistente.trim().equalsIgnoreCase(arma.getNombre()))
                return false;  // Ya posee un arma con el mismo nombre
        }

        for (int i = 0; i < this.arregloArmas.length; i++) {
            if (this.arregloArmas[i] == null) {
                this.arregloArmas[i] = arma;
                return true;
            }
        }
        return false;  // No hay espacios disponibles
    }

    public Arma buscarArmaPorNombre(String nombreArma) {
        if (nombreArma == null || this.arregloArmas == null)
            return null;
        String buscado = nombreArma.trim().toUpperCase();
        for (Arma arma : this.arregloArmas) {
            if (arma == null)
                continue;
            String actual = arma.getNombre();
            if (actual != null && actual.trim().toUpperCase().equals(buscado))
                return arma;
        }
        return null;
    }

    public boolean tieneArma(String nombreArma) {
        return buscarArmaPorNombre(nombreArma) != null;
    }
    
    // Getters
    public int getVida() {
        return vida;
    }

    public Arma[] getArregloArmas() {
        return arregloArmas;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public boolean getActivo() {
        return activo;
    }
    
    public String getNombre() {
        return name;
    }
}
