/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.fatality;

/**
 *
 * @author kokoju
 */
public class Peleador {
    // Atributos
    int vida;  // Vida del peleador
    Arma[] arregloArmas;  // Arreglo de las armas del peleador
    Tipo tipo;  // Tipo al que pertenece el peleador
    boolean activo;  // Booleano para verificar si el peleador está activo
    
    // Constructor
    public Peleador(Arma[] arregloArmas, Tipo tipo) {  // Como estas caracteristicas las elige el jugador, debemos hacerlo de esta menra
        this.vida = 100;
        this.arregloArmas = arregloArmas;
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
        this.arregloArmas[indiArma].generarNuevoArregloGolpe();  // Para la recarga, se genera un nuevo arreglo de golpe
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
}
