/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

import java.util.LinkedHashMap;

/**
 *
 * @author kokoju
 */
public class Stats {
    // Atributos
    LinkedHashMap<String, Integer> hashMapStats;

    // Constructores
    public Stats() { // Constructor que establece todo en 0s
        this.hashMapStats = new LinkedHashMap<>();
        this.hashMapStats.put("WINS", 0);
        this.hashMapStats.put("LOSSES", 0);
        this.hashMapStats.put("ATTACKS", 0);
        this.hashMapStats.put("SUCCESS", 0);
        this.hashMapStats.put("FAILED", 0);
        this.hashMapStats.put("GIVEUP", 0);
    }

    public Stats(int wins, int losses, int attacks, int sucess, int failed, int giveup) {
        this.hashMapStats = new LinkedHashMap<>();
        this.hashMapStats.put("WINS", wins);
        this.hashMapStats.put("LOSSES", losses);
        this.hashMapStats.put("ATTACKS", attacks);
        this.hashMapStats.put("SUCCESS", sucess);
        this.hashMapStats.put("FAILED", failed);
        this.hashMapStats.put("GIVEUP", giveup);
    }

    // Métodos
    public void incrementarStat(String statIncrementada) { // Incrementa en 1 una estadística
        statIncrementada = statIncrementada.toUpperCase();
        int val = this.hashMapStats.get(statIncrementada);
        this.hashMapStats.put(statIncrementada, ++val);
    }

    public void addAttackSummary(int totalAttacks, int successfulAttacks, int failedAttacks) {
        int val = 0; // Se crea una variable para almacenar enteros
        if (totalAttacks > 0)
            val = this.hashMapStats.get("ATTACKS");
        this.hashMapStats.put("ATTACKS", (val + totalAttacks));
        if (successfulAttacks > 0)
            val = this.hashMapStats.get("SUCCESS");
        this.hashMapStats.put("SUCCESS", (val + successfulAttacks));
        if (failedAttacks > 0)
            val = this.hashMapStats.get("FAILED");
        this.hashMapStats.put("FAILED", (val + failedAttacks));
    }

    // Getters
    public LinkedHashMap<String, Integer> getHashMapStats() {
        return hashMapStats;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("W:").append(hashMapStats.get("WINS"));
        sb.append(" L:").append(hashMapStats.get("LOSSES"));
        sb.append(" A:").append(hashMapStats.get("ATTACKS"));
        sb.append(" S:").append(hashMapStats.get("SUCCESS"));
        sb.append(" F:").append(hashMapStats.get("FAILED"));
        return sb.toString();
    }
}
