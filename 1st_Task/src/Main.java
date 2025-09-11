import java.util.*;

public class Main {
    public static void main(String[] args) {
        new GameEngine().start();
    }

    /* =========================
     * Engine / Flow
     * ========================= */
    static class GameEngine {
        private final Scanner sc = new Scanner(System.in);
        private final Random rng = new Random();

        private Player player;
        private boolean running = true;
        private boolean inGame = false;

        // session stats
        private int turns = 0;
        private int battles = 0;

        public void start() {
            while (running) {
                showMainMenu();
                int sel = readInt("> ");
                switch (sel) {
                    case 1 -> newGame();
                    case 99 -> {
                        println("게임을 종료합니다. 감사합니다.");
                        running = false;
                    }
                    default -> println("잘못된 선택입니다.");
                }
            }
        }

        private void showMainMenu() {
            println("\n=== Tiny Dungeon CLI ===");
            println("1) 새 게임 시작");
            println("99) 종료");
        }

        private void newGame() {
            this.turns = 0;
            this.battles = 0;

            // 초기 플레이어 설정
            this.player = new Player("Rogue", 30, 5, 2);

            // 시작 안내
            println("\n[플레이어 생성]");
            println("이름: " + player.name);
            println("초기 스탯: HP " + player.stats.hp + "/" + player.stats.maxHp +
                    "  ATK " + player.stats.atk + "  DEF " + player.stats.def);
            println("장비: 무기 없음 / 방어구 없음");

            // 게임 루프
            this.inGame = true;
            boolean exitDiscovered = false;
            int kills = 0;

            while (inGame) {
                printSeparator();
                showFloorHUD();
                println("1) 탐색");
                println("2) 상황판(현재 스탯 보기)");
                println("9) 포기하고 나가기");
                int sel = readInt("> ");

                switch (sel) {
                    case 1 -> {
                        turns++;
                        // 70% 전투, 30% 이벤트(출구 발견 가능)
                        if (rng.nextInt(100) < 70) {
                            Enemy enemy = createRandomEnemy();
                            battle(enemy);
                            if (!player.isAlive()) {
                                gameOver(false);
                                return;
                            }
                            kills++;
                            // 두 번 이상 처치하면 출구 발견 확률 상승
                            if (kills >= 2 && rng.nextInt(100) < 50) exitDiscovered = true;
                        } else {
                            if (!exitDiscovered && rng.nextInt(100) < 40) {
                                exitDiscovered = true;
                            }
                            if (exitDiscovered) {
                                println("\n[탐색] 출구를 발견했습니다! 나가시겠습니까?");
                                println("1) 출구로 나간다");
                                println("2) 더 탐색한다");
                                int ex = readInt("> ");
                                if (ex == 1) {
                                    gameOver(true);
                                    return;
                                } else {
                                    println("더 탐색합니다...");
                                }
                            } else {
                                println("\n[탐색] 특이사항 없이 지나쳤습니다.");
                            }
                        }
                    }
                    case 2 -> showStatus();
                    case 9 -> {
                        println("포기하고 나갑니다...");
                        gameOver(false);
                        return;
                    }
                    default -> println("잘못된 선택입니다.");
                }
            }
        }

        private void battle(Enemy enemy) {
            println("\n[탐색] 적을 만났습니다!");
            println("적: " + enemy.name + " (Lv" + enemy.level + ")  HP " + enemy.stats.hp +
                    "  ATK " + enemy.stats.atk + "  DEF " + enemy.stats.def +
                    "  전리품확률 " + (int)(enemy.lootRate * 100) + "%");

            boolean defending = false;
            println("\n[전투 시작]");

            while (player.isAlive() && enemy.isAlive()) {
                // Player turn
                println("\n당신의 턴:");
                println("1) 공격");
                println("2) 방어(이번 적 공격 피해 50% 경감)");
                println("3) 도망 시도");
                int sel = readInt("> ");

                switch (sel) {
                    case 1 -> {
                        int dmg = damageDealt(player.totalAtk(), enemy.stats.def);
                        enemy.takeDamage(dmg);
                        println("당신의 공격! (" + player.totalAtk() + " - " + enemy.stats.def + " → 피해 " + dmg + ")  → "
                                + enemy.name + " HP " + Math.max(0, enemy.stats.hp) + "/" + enemy.stats.maxHp);
                        defending = false;
                    }
                    case 2 -> {
                        println("방어 태세!");
                        defending = true;
                    }
                    case 3 -> {
                        boolean success = rng.nextInt(100) < 40; // 40% 탈출
                        if (success) {
                            println("도망에 성공했습니다! 전투를 종료합니다.");
                            return;
                        } else {
                            println("도망 실패! 적이 선제공격합니다!");
                            int dmg = damageDealt(enemy.stats.atk, player.totalDef());
                            player.takeDamage(dmg);
                            println(enemy.name + "의 선제공격! 피해 " + dmg + " → 당신 HP " +
                                    Math.max(0, player.stats.hp) + "/" + player.stats.maxHp);
                            if (!player.isAlive()) break;
                            defending = false; // 선제공격 후 정상 진행
                        }
                    }
                    default -> {
                        println("잘못된 선택입니다. 공격으로 간주합니다.");
                        int dmg = damageDealt(player.totalAtk(), enemy.stats.def);
                        enemy.takeDamage(dmg);
                        println("당신의 공격! 피해 " + dmg + " → " + enemy.name + " HP " +
                                Math.max(0, enemy.stats.hp) + "/" + enemy.stats.maxHp);
                        defending = false;
                    }
                }

                // Enemy defeated?
                if (!enemy.isAlive()) break;

                // Enemy turn
                int raw = damageDealt(enemy.stats.atk, player.totalDef());
                int dmg = defending ? Math.max(0, (int)Math.ceil(raw * 0.5)) : raw;
                player.takeDamage(dmg);
                println(enemy.name + "의 공격" + (defending ? " (경감)" : "") + "! 피해 " + dmg +
                        " → 당신 HP " + Math.max(0, player.stats.hp) + "/" + player.stats.maxHp);
                defending = false;
                turns++;
            }

            if (player.isAlive()) {
                battles++;
                println("\n[전투 종료] " + enemy.name + " 처치!");
                grantLoot(enemy);
            }
        }

        private void grantLoot(Enemy enemy) {
            if (rng.nextDouble() < enemy.lootRate) {
                // 전리품 유형: 포션 또는 장비 (무기/방어구)
                int roll = rng.nextInt(100);
                if (roll < 50) {
                    // Potion drop
                    int heal = 5 + enemy.level * 3; // 레벨 비례 회복량
                    Potion p = new Potion("회복 포션 +" + heal, heal);
                    println("전리품: " + p.name + " (사용 시 HP+" + p.healAmount + ")");
                    askUsePotionNowOrDiscard(p);
                } else {
                    // Equipment drop (50% 확률)
                    if (rng.nextBoolean()) {
                        int bonus = Math.max(1, enemy.level + rng.nextInt(2));
                        Weapon w = new Weapon("전리품 단검 +" + bonus, bonus);
                        println("전리품: " + w.name + " (Weapon, ATK+" + w.atkBonus + ")");
                        askEquipWeaponNowOrDiscard(w);
                    } else {
                        int bonus = Math.max(1, enemy.level + rng.nextInt(2));
                        Armor a = new Armor("전리품 가죽갑옷 +" + bonus, bonus);
                        println("전리품: " + a.name + " (Armor, DEF+" + a.defBonus + ")");
                        askEquipArmorNowOrDiscard(a);
                    }
                }
            } else {
                println("전리품이 없습니다.");
            }
        }

        private void askUsePotionNowOrDiscard(Potion p) {
            println("지금 사용하시겠습니까? (사용하지 않으면 사라집니다)");
            println("1) 예  2) 아니오(버리기)");
            int sel = readInt("> ");
            if (sel == 1) {
                int before = player.stats.hp;
                player.restoreHp(p.healAmount);
                int after = player.stats.hp;
                println("포션 사용: HP " + before + " → " + after + " (+" + (after - before) + ")");
            } else {
                println("포션을 버렸습니다.");
            }
        }

        private void askEquipWeaponNowOrDiscard(Weapon w) {
            println("지금 장착하시겠습니까? (장착하지 않으면 사라집니다)");
            println("1) 예  2) 아니오(버리기)");
            int sel = readInt("> ");
            if (sel == 1) {
                int before = player.totalAtk();
                Weapon prev = player.weapon;
                player.equipWeapon(w);
                int after = player.totalAtk();
                if (prev != null) println("기존 무기 '" + prev.name + "'을(를) 교체했습니다.");
                println("장착 완료: " + w.name + " (ATK+" + w.atkBonus + ")");
                println("ATK " + before + " → " + after);
            } else {
                println("아이템을 버렸습니다.");
            }
        }

        private void askEquipArmorNowOrDiscard(Armor a) {
            println("지금 장착하시겠습니까? (장착하지 않으면 사라집니다)");
            println("1) 예  2) 아니오(버리기)");
            int sel = readInt("> ");
            if (sel == 1) {
                int before = player.totalDef();
                Armor prev = player.armor;
                player.equipArmor(a);
                int after = player.totalDef();
                if (prev != null) println("기존 방어구 '" + prev.name + "'을(를) 교체했습니다.");
                println("장착 완료: " + a.name + " (DEF+" + a.defBonus + ")");
                println("DEF " + before + " → " + after);
            } else {
                println("아이템을 버렸습니다.");
            }
        }

        private void showStatus() {
            println("\n[상황판]");
            println("HP " + player.stats.hp + "/" + player.stats.maxHp +
                    "  ATK " + player.totalAtk() + "  DEF " + player.totalDef());
            println("장비: Weapon=" + (player.weapon == null ? "없음" : player.weapon.name + "(+" + player.weapon.atkBonus + ")")
                    + "  /  Armor=" + (player.armor == null ? "없음" : player.armor.name + "(+" + player.armor.defBonus + ")"));
            println("전투 " + battles + "회  /  턴 " + turns + "턴");
        }

        private void gameOver(boolean escaped) {
            printSeparator();
            if (!player.isAlive()) {
                println("=== 패배 ===");
            } else if (escaped) {
                println("=== 클리어! ===");
            } else {
                println("=== 종료 ===");
            }
            println("남은 HP: " + Math.max(0, player.stats.hp) + "/" + player.stats.maxHp);
            String w = (player.weapon == null ? "-" : player.weapon.name + "(ATK+" + player.weapon.atkBonus + ")");
            String a = (player.armor == null ? "-" : player.armor.name + "(DEF+" + player.armor.defBonus + ")");
            println("장비: " + w + " / " + a);
            println("전투: " + battles + "회,  총 턴: " + turns + "턴");
            inGame = false;
        }

        /* ========== helpers ========== */

        private Enemy createRandomEnemy() {
            // 간단 난이도: lv 1~3
            int lv = 1 + rng.nextInt(3);
            int baseHp = 8 + lv * (4 + rng.nextInt(3));   // 12~21
            int baseAtk = 2 + lv * (1 + rng.nextInt(2));  // 3~8
            int baseDef = 0 + lv * rng.nextInt(2);        // 0~4
            double loot = 0.30 + lv * 0.12;               // 0.42~0.66 (상향)

            String[] names = {"고블린", "스켈레톤", "늑대인간", "도적", "트로그"};
            String name = names[rng.nextInt(names.length)];
            return new Enemy(name, baseHp, baseAtk, baseDef, lv, Math.min(0.85, loot));
        }

        private int damageDealt(int atk, int def) {
            return Math.max(1, atk - def);
        }

        private int readInt(String prompt) {
            System.out.print(prompt);
            while (true) {
                String line = sc.nextLine().trim();
                try {
                    return Integer.parseInt(line);
                } catch (NumberFormatException e) {
                    System.out.print("숫자를 입력하세요: ");
                }
            }
        }

        private void println(String msg) { System.out.println(msg); }
        private void printSeparator() { System.out.println("\n────────────"); }

        private void showFloorHUD() {
            println("[던전 1층]");
            println("상태: HP " + player.stats.hp + "/" + player.stats.maxHp +
                    "  ATK " + player.totalAtk() + "  DEF " + player.totalDef());
            println("장비: Weapon:" + (player.weapon == null ? "없음" : player.weapon.name + "(+" + player.weapon.atkBonus + ")")
                    + "  Armor:" + (player.armor == null ? "없음" : player.armor.name + "(+" + player.armor.defBonus + ")"));
        }
    }

    /* 스텟과 속성값 */
    static final class Stats {
        int maxHp, hp, atk, def;
        Stats(int maxHp, int atk, int def) {
            this.maxHp = Math.max(1, maxHp);
            this.hp = this.maxHp;
            this.atk = Math.max(0, atk);
            this.def = Math.max(0, def);
        }
        void takeDamage(int amount) { hp = Math.max(0, hp - Math.max(0, amount)); }
        void heal(int amount) { hp = Math.min(maxHp, hp + Math.max(0, amount)); }
        boolean isAlive() { return hp > 0; }
    }

    static class Player {
        String name;
        Stats stats;
        Weapon weapon; // nullable
        Armor armor;   // nullable

        Player(String name, int maxHp, int atk, int def) {
            this.name = name;
            this.stats = new Stats(maxHp, atk, def);
        }

        int totalAtk() { return stats.atk + (weapon == null ? 0 : weapon.atkBonus); }
        int totalDef() { return stats.def + (armor  == null ? 0 : armor .defBonus); }

        boolean isAlive() { return stats.isAlive(); }
        void takeDamage(int dmg) { stats.takeDamage(dmg); }
        void restoreHp(int amount) { stats.heal(amount); }

        void equipWeapon(Weapon w) { this.weapon = w; }
        void equipArmor(Armor a)   { this.armor  = a; }
    }

    static class Enemy {
        String name;
        Stats stats;
        int level;
        double lootRate; // 0.0 ~ 1.0

        Enemy(String name, int maxHp, int atk, int def, int level, double lootRate) {
            this.name = name;
            this.stats = new Stats(maxHp, atk, def);
            this.level = Math.max(1, level);
            this.lootRate = Math.max(0.0, Math.min(1.0, lootRate));
        }

        boolean isAlive() { return stats.isAlive(); }
        void takeDamage(int dmg) { stats.takeDamage(dmg); }
    }

    /* =========================
     * Item Model (2-level inheritance for equipment)
     * ========================= */
    enum EquipSlot { WEAPON, ARMOR }

    static class Item {
        String name;
        Item(String name) { this.name = name; }
    }

    static class Equipment extends Item {
        EquipSlot equipSlot;
        Equipment(String name, EquipSlot slot) {
            super(name);
            this.equipSlot = slot;
        }
    }

    static class Weapon extends Equipment {
        int atkBonus;
        Weapon(String name, int atkBonus) {
            super(name, EquipSlot.WEAPON);
            this.atkBonus = Math.max(0, atkBonus);
        }
    }

    static class Armor extends Equipment {
        int defBonus;
        Armor(String name, int defBonus) {
            super(name, EquipSlot.ARMOR);
            this.defBonus = Math.max(0, defBonus);
        }
    }

    static class Potion extends Item {
        int healAmount;
        Potion(String name, int healAmount) {
            super(name);
            this.healAmount = Math.max(1, healAmount);
        }
    }
}
