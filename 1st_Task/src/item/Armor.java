package item;

public final class Armor extends Equipment {
    private final int defBonus;

    public Armor(String name, int defBonus) {
        super(name);
        this.defBonus = Math.max(0, defBonus);
    }

    public int getDefBonus() {
        return defBonus;
    }
}
