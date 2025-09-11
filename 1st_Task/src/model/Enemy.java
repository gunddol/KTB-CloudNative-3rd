package model;

public final class Enemy {
    private final String name;
    private final Stats stats;
    private final int level;
    private final double lootRate; // 0.0 ~ 1.0

    public Enemy(String name, int maxHp, int atk, int def, int level, double lootRate) {
        this.name = name;
        this.stats = new Stats(maxHp, atk, def);
        this.level = Math.max(1, level);
        this.lootRate = Math.max(0.0, Math.min(1.0, lootRate));
    }

    // 전투 시
    public boolean isAlive() {
        return stats.isAlive();
    }
    public void takeDamage(int dmg) {
        stats.takeDamage(dmg);
    }

    // 접근자
    public String getName() { return name; }
    public int getHp() { return stats.getHp(); }
    public int getMaxHp() { return stats.getMaxHp(); }
    public int getAtk() { return stats.getAtk(); }
    public int getDef() { return stats.getDef(); }
    public int getLevel() { return level; }
    public double getLootRate() { return lootRate; }
}
