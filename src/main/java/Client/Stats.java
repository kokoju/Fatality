/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

/**
 *
 * @author kokoju
 */
public class Stats {
    // Atributos
    int wins;
    int losses;
    int attacks;
    int sucess;
    int failed;
    int giveup;
    
    // Constructores
    public Stats() {  // Constructor que establece todo en 0s
        this.wins = 0;
        this.losses = 0;
        this.attacks = 0;
        this.sucess = 0;
        this.failed = 0;
        this.giveup = 0;
    }
    
    public Stats(int wins, int losses, int attacks, int sucess, int failed, int giveup) {
        this.wins = wins;
        this.losses = losses;
        this.attacks = attacks;
        this.sucess = sucess;
        this.failed = failed;
        this.giveup = giveup;
    }
    
    
    // Métodos
    public void incrementarStat(String statIncrementada) {
        statIncrementada = statIncrementada.toUpperCase();
        switch(statIncrementada) {
            case "WINS" -> ++wins;
            case "LOSSES" -> ++losses;
            case "ATTACKS" -> ++attacks;
            case "SUCESS" -> ++sucess;
            case "FAILED" -> ++failed;
            case "GIVEUP" -> ++giveup;
        }
    }
    
    // Getters
    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getAttacks() {
        return attacks;
    }

    public int getSucess() {
        return sucess;
    }

    public int getFailed() {
        return failed;
    }

    public int getGiveup() {
        return giveup;
    }
    
}
