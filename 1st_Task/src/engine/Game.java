package engine;

import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import config.Balance;
import item.Armor;
import item.Potion;
import item.Weapon;
import model.Enemy;
import model.Player;

public final class Game {

    private static final String SEP = "\n────────────";

    private final Scanner sc = new Scanner(System.in);

    private Player player;
    private int turns;
    private int battles;
    private boolean exitDiscovered;

    public void start() {
        while (true) {
            println("\n===== KTB 던전 탈출 로그라이크 =====");
            println("1) 던전 들어가기");
            println("99) 게임 종료");
            int sel = readInt("> ");
            switch (sel) {
                case 1:
                    newGame();
                    break;
                case 99:
                    println("게임을 종료합니다. 감사합니다.");
                    return;
                default:
                    println("잘못된 선택입니다.");
            }
        }
    }

    private void newGame() {
        this.turns = 0;
        this.battles = 0;
        this.exitDiscovered = false;

        this.player = new Player("Caleb", 30, 5, 3);

        println("\n[플레이어 생성]");
        println(String.format("이름: %s", player.getName()));
        println(String.format("초기 스탯: HP %d/%d  ATK %d  DEF %d",
                player.getHp(), player.getMaxHp(), player.getBaseAtk(), player.getBaseDef()));
        println("장비: 무기 없음 / 방어구 없음");

        int kills = 0;

        while (player.isAlive()) {
            printSep();
            showHUD();

            println("1) 탐색");
            println("2) 상태창(현재 스탯 보기)");
            println("9) 포기하고 나가기");
            int sel = readInt("> ");

            switch (sel) {
                case 1:
                    turns++;
                    if (chance(Balance.CHANCE_BATTLE)) {
                        Enemy e = createRandomEnemy();
                        battle(e);
                        if (!player.isAlive()) break;
                        kills++;
                        if (kills >= Balance.EXIT_UNLOCK_KILLS && chance(Balance.EXIT_FIND_BONUS)) {
                            exitDiscovered = true;
                        }
                    } else {
                        if (!exitDiscovered && chance(Balance.EXIT_FIND_BASE)) {
                            exitDiscovered = true;
                        }
                        handleExplorationEvent();
                    }
                    break;
                case 2:
                    showStatus();
                    break;
                case 9:
                    println("포기하고 나갑니다...");
                    gameOver(false);
                    return;
                default:
                    println("잘못된 선택입니다.");
            }
        }

        if (!player.isAlive()) {
            gameOver(false);
        }
    }

    private void handleExplorationEvent() {
        if (exitDiscovered) {
            println("\n[탐색] 출구를 발견했습니다! 나가시겠습니까?");
            println("1) 출구로 나간다");
            println("2) 더 탐색한다");
            int ex = readInt("> ");
            if (ex == 1) {
                gameOver(true);
            } else {
                println("더 탐색합니다...");
            }
        } else {
            println("\n[탐색] 특이사항 없이 지나쳤습니다.");
        }
    }

    private void battle(Enemy enemy) {
        println("\n[탐색] 적을 만났습니다!");
        println(String.format("적: %s (Lv%d)  HP %d  ATK %d  DEF %d  전리품확률 %d%%",
                enemy.getName(), enemy.getLevel(), enemy.getHp(),
                enemy.getAtk(), enemy.getDef(), (int) (enemy.getLootRate() * 100)));

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
                case 1: {
                    int dmg = deal(player.getTotalAtk(), enemy.getDef());
                    enemy.takeDamage(dmg);
                    println(String.format("당신의 공격! (%d - %d → 피해 %d) → %s HP %d/%d",
                            player.getTotalAtk(), enemy.getDef(), dmg,
                            enemy.getName(), Math.max(0, enemy.getHp()), enemy.getMaxHp()));
                    defending = false;
                    break;
                }
                case 2:
                    println("방어 태세!");
                    defending = true;
                    break;
                case 3:
                    if (chance(Balance.CHANCE_ESCAPE)) {
                        println("도망에 성공했습니다! 전투를 종료합니다.");
                        return;
                    } else {
                        int dmg = deal(enemy.getAtk(), player.getTotalDef());
                        player.takeDamage(dmg);
                        println(String.format("도망 실패! %s의 선제공격! 피해 %d → 당신 HP %d/%d",
                                enemy.getName(), dmg, Math.max(0, player.getHp()), player.getMaxHp()));
                        if (!player.isAlive()) break;
                        defending = false;
                    }
                    break;
                default: {
                    int dmg = deal(player.getTotalAtk(), enemy.getDef());
                    enemy.takeDamage(dmg);
                    println(String.format("잘못된 선택입니다. 공격으로 간주합니다. 피해 %d → %s HP %d/%d",
                            dmg, enemy.getName(), Math.max(0, enemy.getHp()), enemy.getMaxHp()));
                    defending = false;
                    break;
                }
            }

            if (!enemy.isAlive()) break;

            // Enemy turn
            int raw = deal(enemy.getAtk(), player.getTotalDef());
            int dmg = defending ? Math.max(0, (int) Math.ceil(raw * Balance.DEFEND_REDUCTION)) : raw;
            player.takeDamage(dmg);
            println(String.format("%s의 공격%s! 피해 %d → 당신 HP %d/%d",
                    enemy.getName(), (defending ? " (경감)" : ""), dmg,
                    Math.max(0, player.getHp()), player.getMaxHp()));
            defending = false;
            turns++;
        }

        if (player.isAlive()) {
            battles++;
            println(String.format("\n[전투 종료] %s 처치!", enemy.getName()));
            grantLoot(enemy);
        }
    }

    private void grantLoot(Enemy enemy) {
        if (!chancePercent(enemy.getLootRate())) {
            println("전리품이 없습니다.");
            return;
        }
        // 50% 포션, 50% 장비(무기/방어구 50:50)
        if (randInt(100) < 50) {
            int heal = Balance.POTION_BASE + enemy.getLevel() * Balance.POTION_PER_LEVEL;
            Potion p = new Potion("회복 포션 +" + heal, heal);
            println(String.format("전리품: %s (사용 시 HP+%d)", p.getName(), p.getHealAmount()));
            println("지금 사용하시겠습니까? (사용하지 않으면 사라집니다)");
            println("1) 예  2) 아니오(버리기)");
            int sel = readInt("> ");
            if (sel == 1) {
                int before = player.getHp();
                player.restoreHp(p.getHealAmount());
                int after = player.getHp();
                println(String.format("포션 사용: HP %d → %d ( +%d )", before, after, (after - before)));
            } else {
                println("포션을 버렸습니다.");
            }
        } else {
            boolean weaponDrop = randInt(2) == 0;
            if (weaponDrop) {
                int bonus = Math.max(1, enemy.getLevel() + randInt(2));
                Weapon w = new Weapon("전리품 단검 +" + bonus, bonus);
                println(String.format("전리품: %s (ATK+%d)", w.getName(), w.getAtkBonus()));
                println("지금 장착하시겠습니까? (장착하지 않으면 사라집니다)");
                println("1) 예  2) 아니오(버리기)");
                int sel = readInt("> ");
                if (sel == 1) {
                    int before = player.getTotalAtk();
                    Weapon prev = player.getWeapon();
                    player.equipWeapon(w);
                    int after = player.getTotalAtk();
                    if (prev != null) println(String.format("기존 무기 '%s' 교체", prev.getName()));
                    println(String.format("장착 완료: %s  ATK %d → %d", w.getName(), before, after));
                } else {
                    println("아이템을 버렸습니다.");
                }
            } else {
                int bonus = Math.max(1, enemy.getLevel() + randInt(2));
                Armor a = new Armor("전리품 가죽갑옷 +" + bonus, bonus);
                println(String.format("전리품: %s (DEF+%d)", a.getName(), a.getDefBonus()));
                println("지금 장착하시겠습니까? (장착하지 않으면 사라집니다)");
                println("1) 예  2) 아니오(버리기)");
                int sel = readInt("> ");
                if (sel == 1) {
                    int before = player.getTotalDef();
                    Armor prev = player.getArmor();
                    player.equipArmor(a);
                    int after = player.getTotalDef();
                    if (prev != null) println(String.format("기존 방어구 '%s' 교체", prev.getName()));
                    println(String.format("장착 완료: %s  DEF %d → %d", a.getName(), before, after));
                } else {
                    println("아이템을 버렸습니다.");
                }
            }
        }
    }

    private void showStatus() {
        println("\n[상태창]");
        println(String.format("HP %d/%d  ATK %d  DEF %d",
                player.getHp(), player.getMaxHp(), player.getTotalAtk(), player.getTotalDef()));
        println(String.format("장비: Weapon=%s  /  Armor=%s",
                (player.getWeapon() == null ? "없음" : player.getWeapon().getName() + "(+" + player.getWeapon().getAtkBonus() + ")"),
                (player.getArmor() == null ? "없음" : player.getArmor().getName() + "(+" + player.getArmor().getDefBonus() + ")")));
        println(String.format("전투 %d회  /  턴 %d턴", battles, turns));
    }

    private void gameOver(boolean cleared) {
        printSep();
        if (!player.isAlive()) {
            println("=== 패배 ===");
        } else if (cleared) {
            println("=== 클리어! ===");
        } else {
            println("=== 종료 ===");
        }
        println(String.format("남은 HP: %d/%d", Math.max(0, player.getHp()), player.getMaxHp()));
        String w = (player.getWeapon() == null ? "-" : player.getWeapon().getName() + "(ATK+" + player.getWeapon().getAtkBonus() + ")");
        String a = (player.getArmor() == null ? "-" : player.getArmor().getName() + "(DEF+" + player.getArmor().getDefBonus() + ")");
        println("장비: " + w + " / " + a);
        println(String.format("전투: %d회,  총 턴: %d턴", battles, turns));
    }

    // ===== 유틸 =====
    private static boolean chance(int percent) { return randInt(100) < percent; }
    private static boolean chancePercent(double p) { return ThreadLocalRandom.current().nextDouble() < p; }
    private static int randInt(int boundExclusive) { return ThreadLocalRandom.current().nextInt(boundExclusive); }
    private static int deal(int atk, int def) { return Math.max(1, atk - def); }
    private static void println(String m) { System.out.println(m); }
    private static void printSep() { System.out.println(SEP); }

    private int readInt(String prompt) {
        System.out.print(prompt);
        while (true) {
            String s = sc.nextLine().trim();
            try { return Integer.parseInt(s); }
            catch (NumberFormatException e) { System.out.print("숫자를 입력하세요: "); }
        }
    }

    private void showHUD() {
        println("[던전 1층]");
        println(String.format("상태: HP %d/%d  ATK %d  DEF %d",
                player.getHp(), player.getMaxHp(), player.getTotalAtk(), player.getTotalDef()));
        println(String.format("장비: Weapon:%s  Armor:%s",
                (player.getWeapon() == null ? "없음" : player.getWeapon().getName() + "(+" + player.getWeapon().getAtkBonus() + ")"),
                (player.getArmor() == null ? "없음" : player.getArmor().getName() + "(+" + player.getArmor().getDefBonus() + ")")));
    }

    private Enemy createRandomEnemy() {
        int lv = Balance.LV_MIN + randInt(Balance.LV_RANGE); // 1..3
        int baseHp = Balance.HP_BASE + lv * (Balance.HP_PER_LV_MIN + randInt(Balance.HP_PER_LV_RAND_BOUND)); // 0..3
        int baseAtk = Balance.ATK_BASE + lv * (Balance.ATK_PER_LV_MIN + randInt(Balance.ATK_PER_LV_RAND_BOUND)); // 0..2
        int baseDef = (lv == 1)
                ? randInt(Balance.DEF_LOW_BOUND)                           // 0..1
                : Balance.DEF_HIGH_MIN + randInt(Balance.DEF_HIGH_BOUND);  // 1..2

        double loot = Math.min(Balance.LOOT_RATE_MAX, 0.30 + lv * 0.12);

        String name = Balance.ENEMY_NAMES[randInt(Balance.ENEMY_NAMES.length)];
        return new Enemy(name, baseHp, baseAtk, Math.max(0, baseDef), lv, loot);
    }
}
