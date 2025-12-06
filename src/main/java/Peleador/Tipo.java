/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package Peleador;

/**
 *
 * @author kokoju
 */
public enum Tipo {  // Tipos que puede tener un luchador, elegidos por el usuario (se puede usar un ComboBox)
    FUEGO,
    AIRE,
    AGUA,
    TIERRA,
    VOLADOR,
    VENENO,
    HIELO,
    METAL,
    PSIQUICO,
    HADA;
    
    @Override
    public String toString() {  // Al llamar al toString, se devuelve el nombre con solo la primera mayúscula (FUEGO -> Fuego)
       return (this.name().substring(0, 1) + (this.name().substring(1)).toLowerCase());  // .substring parte una cadena: recibe un parámetro de inicio y un parámetro de fin (no incluido). Si no se pone un 2do parámetro, llega hasta el final de la cadena
    }
};