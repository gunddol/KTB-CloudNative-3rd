package config;

public final class Balance {
    private Balance() {}

    // 전투/탐색 확률
    public static final int CHANCE_BATTLE       = 70;   // 탐색 시 전투 확률(%)
    public static final int CHANCE_ESCAPE       = 40;   // 도망 성공 확률(%)
    public static final double DEFEND_REDUCTION = 0.5;  // 방어 시 피해 경감 비율

    // 출구 발견 로직
    public static final int EXIT_UNLOCK_KILLS   = 2;    // 처치 n회 이상 시 보너스 확률 적용
    public static final int EXIT_FIND_BASE      = 40;   // 전투 아닌 탐색에서 출구 발견 기본 확률(%)
    public static final int EXIT_FIND_BONUS     = 50;   // 보너스 확률(%)

    // 드랍/포션/장비
    public static final double LOOT_RATE_MAX    = 0.85; // 전리품 확률 상한
    public static final int POTION_BASE         = 5;    // 포션 기본 회복
    public static final int POTION_PER_LEVEL    = 3;    // 레벨당 추가 회복

    // 적 생성 파라미터
    public static final int LV_MIN              = 1;    // 최소 레벨
    public static final int LV_RANGE            = 3;    // 레벨 개수(1~3)
    public static final int HP_BASE             = 8;
    public static final int HP_PER_LV_MIN       = 4;
    public static final int HP_PER_LV_RAND_BOUND= 4;    // 0~3
    public static final int ATK_BASE            = 2;
    public static final int ATK_PER_LV_MIN      = 1;
    public static final int ATK_PER_LV_RAND_BOUND=3;    // 0~2
    public static final int DEF_LOW_BOUND       = 2;    // Lv1: 0~1
    public static final int DEF_HIGH_MIN        = 1;    // Lv>1: 1 + 0~1
    public static final int DEF_HIGH_BOUND      = 2;    // 0~1

    // 전투 턴 타임아웃(초) — 시간 내 입력 없으면 기본 동작(방어) 수행
    public static final int TURN_TIMEOUT_SECONDS = 10;

    // 몹 이름 리스트
    public static final String[] ENEMY_NAMES = {
            "고블린", "스켈레톤", "늑대인간", "도적", "트롤"
    };
}
