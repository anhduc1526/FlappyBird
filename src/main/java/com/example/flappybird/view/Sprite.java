package com.example.flappybird.view;

/**
 * VIEW helper — Enum thay thế SpriteSheet (class với static String).
 *
 * Lợi ích so với phiên bản cũ (SpriteSheet):
 *  - Type-safe: truyền Sprite thay vì String → không thể typo đường dẫn ở call-site
 *  - IDE autocomplete và refactor hoạt động chính xác
 *  - Không mất khả năng dùng làm key trong EnumMap / EnumSet
 *
 * Với bird frames và numbers (có index), dùng static helper methods.
 * Với medals cũng vậy.
 *
 * Design Pattern: Type-safe constants (enum).
 */
public enum Sprite {

    // ── Backgrounds & land ────────────────────────────────────────────────────
    BG_DAY   ("/com/example/flappybird/img/background-day.png"),
    BG_NIGHT ("/com/example/flappybird/img/background-night.png"),
    LAND     ("/com/example/flappybird/img/land.png"),

    // ── Pipes ─────────────────────────────────────────────────────────────────
    PIPE_UPPER ("/com/example/flappybird/img/pipe_upper.png"),
    PIPE_LOWER ("/com/example/flappybird/img/pipe_under.png"),

    // ── HUD ───────────────────────────────────────────────────────────────────
    MESSAGE   ("/com/example/flappybird/img/message.png"),
    GAMEOVER  ("/com/example/flappybird/img/TTgameOver.png"),
    PAUSE_BTN ("/com/example/flappybird/img/pause.png"),
    PAUSE_TAB ("/com/example/flappybird/img/pauseTab.png"),
    RESUME    ("/com/example/flappybird/img/resume.png"),
    REPLAY    ("/com/example/flappybird/img/replay.png"),
    BUTTON    ("/com/example/flappybird/img/button.png"),
    ARROW_L   ("/com/example/flappybird/img/nextLeft.png"),
    ARROW_R   ("/com/example/flappybird/img/nextRight.png"),
    SOUND_ON  ("/com/example/flappybird/img/soundon.png"),
    SOUND_OFF ("/com/example/flappybird/img/soundoff.png"),

    // ── Audio ─────────────────────────────────────────────────────────────────
    SFX_CLICK ("/com/example/flappybird/audio/mouse-click.mp3"),
    SFX_FLAP  ("/com/example/flappybird/audio/flap.mp3"),
    SFX_DEAD  ("/com/example/flappybird/audio/dead.mp3"),
    SFX_POINT ("/com/example/flappybird/audio/point.wav"),
    MUSIC     ("/com/example/flappybird/audio/music.mp3");

    // ── Fields ────────────────────────────────────────────────────────────────
    public final String path;

    Sprite(String path) {
        this.path = path;
    }

    // ── Indexed helpers (bird frames, numbers, medals) ────────────────────────

    /** Bird up-flap frames: type 0=yellow, 1=red, 2=blue. */
    public static String birdUp(int type) {
        return birdPath(type, "upflap");
    }

    /** Bird mid-flap frames. */
    public static String birdMid(int type) {
        return birdPath(type, "midflap");
    }

    /** Bird down-flap frames. */
    public static String birdDown(int type) {
        return birdPath(type, "downflap");
    }

    private static final String[] BIRD_PREFIXES = { "yellow", "red", "blue" };

    private static String birdPath(int type, String flap) {
        return "/com/example/flappybird/img/" + BIRD_PREFIXES[type] + "bird-" + flap + ".png";
    }

    /** Large digit 0–9. */
    public static String largeNum(int digit) {
        return "/com/example/flappybird/img/BigNum/" + digit + ".png";
    }

    /** Small digit 0–9. */
    public static String smallNum(int digit) {
        return "/com/example/flappybird/img/SmallNum/" + digit + ".png";
    }

    /** Medal: 0=bronze, 1=silver, 2=gold. */
    public static String medal(int index) {
        return "/com/example/flappybird/img/medal/" + index + ".png";
    }
}