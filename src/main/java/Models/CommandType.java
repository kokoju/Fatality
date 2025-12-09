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
    ATTACK (6),  //attack <Target> <Peleador> <Arma> <Opcional Peleador> <opcional Arma>
    APPLYATTACK (3), //
    RESULT (3), // result notifications: RESULT <recipient/optional> <message>
    MESSAGE (2), //message hola a todos
    PRIVATE_MESSAGE(3), //private Andres hola andres
    GIVEUP (1), //giveup
    NAME (2),
    NAME_HANDSHAKE (3),
    CREATEFIGHTER (3),
    ASSIGNWEAPON (3),
    UPDATESUMMARY (4),
    UPDATESTATS (4), // UPDATESTATS ranking ownStats enemyStats
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
