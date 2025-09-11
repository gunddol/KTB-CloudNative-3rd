package item;

public final class Weapon extends Equipment {
    private final int atkBonus;

    public Weapon(String name, int atkBonus) {
        super(name);
        this.atkBonus = Math.max(0, atkBonus);
    }

    public int getAtkBonus() {
        return atkBonus;
    }
}
