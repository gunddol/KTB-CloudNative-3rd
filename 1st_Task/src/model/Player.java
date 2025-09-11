package model;

import item.Armor;
import item.Weapon;

public final class Player {
    private final String name;
    private final Stats stats;
    private Weapon weapon; // nullable
    private Armor armor;   // nullable

    public Player(String name, int maxHp, int atk, int def) {
        this.name = name;
        this.stats = new Stats(maxHp, atk, def);
    }

    // 전투 시
    public boolean isAlive() {
        return stats.isAlive();
    }
    public void takeDamage(int dmg) {
        stats.takeDamage(dmg);
    }
    public void restoreHp(int amount) {
        stats.heal(amount);
    }

    // 장비
    public void equipWeapon(Weapon w) {
        this.weapon = w;
    }
    public void equipArmor(Armor a)   {
        this.armor  = a;
    }

    // 파생 스탯
    public int getTotalAtk() {
        return stats.getAtk() + (weapon == null ? 0 : weapon.getAtkBonus());
    }
    public int getTotalDef() {
        return stats.getDef() + (armor  == null ? 0 : armor .getDefBonus());
    }

    // 접근자
    public String getName() { return name; }
    public int getHp() { return stats.getHp(); }
    public int getMaxHp() { return stats.getMaxHp(); }
    public int getBaseAtk() { return stats.getAtk(); }
    public int getBaseDef() { return stats.getDef(); }
    public Weapon getWeapon() { return weapon; }
    public Armor getArmor() { return armor; }
}
