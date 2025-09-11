package item;

public final class Potion extends Item {
    private final int healAmount;

    public Potion(String name, int healAmount) {
        super(name);
        this.healAmount = Math.max(1, healAmount);
    }

    public int getHealAmount() {
        return healAmount;
    }
}
