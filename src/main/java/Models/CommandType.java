/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package Models;

/**
 *
 * @author diego
 */
public enum CommandType {
    LOG (1),
    ATTACK (5),  //attack Andres 4 5
    APPLYATTACK (4), //
    RESULT (3), // result notifications: RESULT <recipient/optional> <message>
    MESSAGE (2), //message hola a todos
    PRIVATE_MESSAGE(3), //private Andres hola andres
    GIVEUP (1), //giveup
    NAME (2),
    SKIP (1),
    NEXT (1),
    READY (1),   //ready  -> para iniciar juego
    BOOST (3); //BOOST <HERO> <HEAL/PROTECT/STRENGTHEN>
    //.. AGREGARÍAN MÁS TIPOS DE COMANDO
    
    
    private int requiredParameters;

    private CommandType(int requiredParameters) {
        this.requiredParameters = requiredParameters;
    }

    public int getRequiredParameters() {
        return requiredParameters;
    }
    
    
    
    
    
}
