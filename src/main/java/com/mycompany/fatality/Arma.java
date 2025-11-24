/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.fatality;

import static com.mycompany.fatality.GameLogic.*;
import java.util.Random;

/**
 *
 * @author kokoju
 */
public class Arma {
    // Atributos
    String nombre;  // Nombre que se le da al arma
    int[] arregloGope;  // Arreglo de 10 posiciones para saber el golpe para cada tipo
    boolean fueUsada;  // Booleano que lleva constancia del uso del arma
    
    public Arma(String nombre) {
        this.nombre = nombre;  // El nombre es la única carácteristica elegible
        this.arregloGope = generarArregloGolpe();
        this.fueUsada = false;  // El arma no ha sido usada cuando se crea, entonces se establece en false
    }
    
    // Métodos
    public int[] generarArregloGolpe() {
        Random rand = new Random();
        int[] arregloObtenido = new int[CANTIDAD_TIPOS];  // Se crea el arreglo en función de la cantidad de tipos existentes
        for (int i = 0; i < arregloObtenido.length; i++) {
            arregloObtenido[i] = rand.nextInt(20, 101);  // Se crea un daño para cada espacio del arreglo, yendo de 20 a 100
        }
        return arregloObtenido;
    }
    
    // Getters
    public String getNombre() {
        return nombre;
    }

    public int[] getArregloGope() {
        return arregloGope;
    }

    public boolean isFueUsada() {
        return fueUsada;
    }
}
