/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;

/**
 *
 * @author diego
 */
public class CommandFactory {
    
    
    public static Command getCommand(String[] args){
        String type = args[0].toUpperCase();
        
        switch (type) {
            case "ATTACK":
                return new CommandAttack(args);
            case "APPLYATTACK":
                return new CommandApplyAttack(args);
            case "MESSAGE":
                return new CommandMessage(args);
            case "RESULT":
                return new CommandResult(args);
            case "PRIVATE_MESSAGE":
                return new CommandPrivateMessage(args);
            case "GIVEUP":
                return new CommandGiveup(args);
            case "DRAW":
                return new CommandMutualDraw(args);
            case "NAME":
                return new CommandName(args);
            case "NAME_HANDSHAKE":
                return new CommandNameHandshake(args);
            case "CREATEFIGHTER":
                return new CommandCreateFighter(args);
            case "ASSIGNWEAPON":
                return new CommandAssignWeapon(args);
            case "RELOAD":
                return new CommandReload(args);
            case "COMODINSTATUS":
                return new CommandComodinStatus(args);
            case "SELECTPLAYER":
                return new CommandSelectPlayer(args);
            case "LOG":
                return new CommandLog(args);
            case "READY":
                return new CommandReady(args);
            case "NEXT":
                return new CommandNextTurn(args);
            case "SKIP":
                return new CommandSkip(args);
            default:
                return null;
        }
        
        
    }
    
}
