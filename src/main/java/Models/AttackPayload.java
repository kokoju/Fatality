package Models;

import java.io.Serializable;
import java.util.Arrays;
import Hero.HeroPackage;

/**
 * DTO serializable para describir un ataque entre clientes/servidor.
 */
public class AttackPayload implements Serializable {
    private String attackerName;
    private String targetName;
    private String heroType;
    private String heroName;
    private String attackType;
    private String[] extras; // parámetros adicionales (coords, radios, etc.)
    private Hero.HeroPackage heroPackage;
    private boolean reflected; // indica si este payload es el resultado de un reflejo

    public AttackPayload(String attackerName, String targetName, String heroType, String heroName, String attackType, String[] extras) {
        this(attackerName, targetName, heroType, heroName, attackType, extras, null, false);
    }

    public AttackPayload(String attackerName, String targetName, String heroType, String heroName, String attackType, String[] extras, HeroPackage heroPackage) {
        this(attackerName, targetName, heroType, heroName, attackType, extras, heroPackage, false);
    }

    public AttackPayload(String attackerName, String targetName, String heroType, String heroName, String attackType, String[] extras, HeroPackage heroPackage, boolean reflected) {
        this.attackerName = attackerName;
        this.targetName = targetName;
        this.heroType = heroType;
        this.heroName = heroName;
        this.attackType = attackType;
        this.extras = extras == null ? new String[0] : extras;
        this.heroPackage = heroPackage;
        this.reflected = reflected;
    }

    public String getAttackerName() {    return attackerName; }
    public String getTargetName() {      return targetName; }
    public String getHeroType() {        return heroType; }
    public String getHeroName() {        return heroName; }
    public String getAttackType() {      return attackType; }
    public String[] getExtras() {        return extras; }
    public HeroPackage getHeroPackage(){ return heroPackage; }
    public boolean isReflected() { return reflected; }

}
