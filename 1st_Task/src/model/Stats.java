package model;

public final class Stats {
    private int maxHp;
    private int hp;
    private int atk;
    private int def;

    public Stats(int maxHp, int atk, int def) {
        this.maxHp = Math.max(1, maxHp);
        this.hp = this.maxHp;
        this.atk = Math.max(0, atk);
        this.def = Math.max(0, def);
    }

    // 상태
    public boolean isAlive() {
        return hp > 0;
    }
    public void takeDamage(int amount) {
        hp = Math.max(0, hp - Math.max(0, amount));
    }
    public void heal(int amount) {
        hp = Math.min(maxHp, hp + Math.max(0, amount));
    }

    // get stats
    public int getMaxHp() { return maxHp; }
    public int getHp() { return hp; }
    public int getAtk() { return atk; }
    public int getDef() { return def; }

    // set stats
    public void setAtk(int atk) {
        this.atk = Math.max(0, atk);
    }
    public void setDef(int def) {
        this.def = Math.max(0, def);
    }
}
