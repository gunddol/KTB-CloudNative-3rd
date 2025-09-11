package engine;

import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import config.Balance;
import item.Armor;
import item.Potion;
import item.Weapon;
import model.Enemy;
import model.Player;

public final class Game {

    private static final String SEP = "\n────────────";

    // 입력/큐(간단 방식): 입력 스레드 1개가 큐에 넣고, 메인은 poll(timeout)으로 받음
    private final Scanner scanner = new Scanner(System.in);
    private final LinkedBlockingQueue<String> inputQ = new LinkedBlockingQueue<>();
    private volatile boolean running = true;

    private Player player;
    private int turns;
    private int battles;
    private boolean exitDiscovered;

    public void start() {
        // 입력 전용 스레드 시작
        Thread inThread = new Thread(() -> {
            while (running) {
                try {
                    if (!scanner.hasNextLine()) break;
                    String line = scanner.nextLine();
                    inputQ.offer(line.trim());
                } catch (Exception e) { break; }
            }
        }, "input-thread");
        inThread.setDaemon(true);
        inThread.start();

        // 메인 메뉴 루프
        while (true) {
            println("\n===== KTB 던전 탈출 로그라이크 =====");
            println("1) 던전 들어가기");
            println("99) 게임 종료");
            int sel = readIntNoTimeout("> ");  // 타임아웃 없음
            switch (sel) {
                case 1:
                    newGame();
                    break;
                case 99:
                    println("게임을 종료합니다. 감사합니다.");
                    running = false; // 입력 스레드 종료 신호
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
            int sel = readIntNoTimeout("> ");

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
            int ex = readIntNoTimeout("> ");
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
                enemy.getAtk(), enemy.getDef(), (int)(enemy.getLootRate()*100)));

        boolean defending = false;
        println("\n[전투 시작]");

        while (player.isAlive() && enemy.isAlive()) {
            // Player Turn : 여기서 타임아웃 적용
            println("\n당신의 턴:");
            println(String.format("1) 공격  2) 방어(50%% 경감)  3) 도망 시도  [입력 제한 %ds]", Balance.TURN_TIMEOUT_SECONDS));

            // 시간 내 입력 없으면 "방어"로 처리
            int choice = readIntWithTimeout("> ", Balance.TURN_TIMEOUT_SECONDS, 2);

            switch (choice) {
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
                default:
                    println("입력이 올바르지 않아 '방어'로 처리합니다.");
                    defending = true;
            }

            if (!enemy.isAlive()) break;

            // Enemy Turn
            int raw = deal(enemy.getAtk(), player.getTotalDef());
            int dmg = defending ? Math.max(0, (int)Math.ceil(raw * Balance.DEFEND_REDUCTION)) : raw;
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
        // 포션:무기:방어구 = 50:25:25
        if (randInt(100) < 50) {
            int heal = Balance.POTION_BASE + enemy.getLevel() * Balance.POTION_PER_LEVEL;
            Potion p = new Potion("회복 포션 +" + heal, heal);
            println(String.format("전리품: %s (사용 시 HP+%d)", p.getName(), p.getHealAmount()));
            println("지금 사용하시겠습니까? (사용하지 않으면 사라집니다)");
            println("1) 예  2) 아니오(버리기)");
            int sel = readIntNoTimeout("> ");
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
                int sel = readIntNoTimeout("> ");
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
                int sel = readIntNoTimeout("> ");
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

    // 입력 유틸
    // 타임아웃 없는 숫자 입력: 입력 스레드가 큐에 넣은 줄을 blocking take()
    private int readIntNoTimeout(String prompt) {
        System.out.print(prompt);
        while (true) {
            try {
                String s = inputQ.take();
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.print("숫자를 입력하세요: ");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return -1;
            }
        }
    }

    // 타임아웃 있는 숫자 입력: timeout 내에 못 받으면 defaultVal 반환
    private int readIntWithTimeout(String prompt, int timeoutSec, int defaultVal) {
        System.out.print(prompt);
        while (true) {
            try {
                String s = inputQ.poll(timeoutSec, TimeUnit.SECONDS);
                if (s == null) {
                    System.out.println("\n(시간 초과) 자동으로 '방어'를 선택합니다.");
                    return defaultVal;
                }
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.print("숫자를 입력하세요: ");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return defaultVal;
            }
        }
    }

    // 기타 유틸, 표시, 난수
    private static boolean chance(int percent) { return randInt(100) < percent; }
    private static boolean chancePercent(double p) { return ThreadLocalRandom.current().nextDouble() < p; }
    private static int randInt(int boundExclusive) { return ThreadLocalRandom.current().nextInt(boundExclusive); }
    private static int deal(int atk, int def) { return Math.max(1, atk - def); }
    private static void println(String m) { System.out.println(m); }
    private static void printSep() { System.out.println(SEP); }

    private void showHUD() {
        println("[던전 1층]");
        println(String.format("상태: HP %d/%d  ATK %d  DEF %d",
                player.getHp(), player.getMaxHp(), player.getTotalAtk(), player.getTotalDef()));
        println(String.format("장비: Weapon:%s  Armor:%s",
                (player.getWeapon() == null ? "없음" : player.getWeapon().getName() + "(+" + player.getWeapon().getAtkBonus() + ")"),
                (player.getArmor() == null ? "없음" : player.getArmor().getName() + "(+" + player.getArmor().getDefBonus() + ")")));
    }

    // 상태창 : 현재 스탯/장비/전투·턴 수
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

    private Enemy createRandomEnemy() {
        int lv = Balance.LV_MIN + randInt(Balance.LV_RANGE); // 1..3
        int baseHp = Balance.HP_BASE + lv * (Balance.HP_PER_LV_MIN + randInt(Balance.HP_PER_LV_RAND_BOUND));
        int baseAtk = Balance.ATK_BASE + lv * (Balance.ATK_PER_LV_MIN + randInt(Balance.ATK_PER_LV_RAND_BOUND));
        int baseDef = (lv == 1)
                ? randInt(Balance.DEF_LOW_BOUND)
                : Balance.DEF_HIGH_MIN + randInt(Balance.DEF_HIGH_BOUND);

        double loot = Math.min(Balance.LOOT_RATE_MAX, 0.30 + lv * 0.12);

        String name = Balance.ENEMY_NAMES[randInt(Balance.ENEMY_NAMES.length)];
        return new Enemy(name, baseHp, baseAtk, Math.max(0, baseDef), lv, loot);
    }
}
