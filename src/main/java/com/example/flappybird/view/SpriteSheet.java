package com.example.flappybird.view;

/**
 * VIEW helper — all resource path constants.
 *
 * Mirrors every texture/sound path referenced in the original C++ project.
 * Paths are relative to /com/example/flappybird/ in src/main/resources.
 */
public final class SpriteSheet {
    private SpriteSheet() {}

    // Bird sprites  (3 types × 3 frames)
    public static final String[] BIRD_UP = {
        "/com/example/flappybird/Picture/yellowbird-upflap.png",
        "/com/example/flappybird/Picture/redbird-upflap.png",
        "/com/example/flappybird/Picture/bluebird-upflap.png"
    };
    public static final String[] BIRD_MID = {
        "/com/example/flappybird/Picture/yellowbird-midflap.png",
        "/com/example/flappybird/Picture/redbird-midflap.png",
        "/com/example/flappybird/Picture/bluebird-midflap.png"
    };
    public static final String[] BIRD_DOWN = {
        "/com/example/flappybird/Picture/yellowbird-downflap.png",
        "/com/example/flappybird/Picture/redbird-downflap.png",
        "/com/example/flappybird/Picture/bluebird-downflap.png"
    };

    // Pipes
    public static final String PIPE_UPPER = "/com/example/flappybird/Picture/pipe_upper.png";
    public static final String PIPE_LOWER = "/com/example/flappybird/Picture/pipe_under.png";

    // Backgrounds & land
    public static final String BG_DAY   = "/com/example/flappybird/Picture/background-day.png";
    public static final String BG_NIGHT = "/com/example/flappybird/Picture/background-night.png";
    public static final String LAND     = "/com/example/flappybird/Picture/land.png";

    // HUD
    public static final String MESSAGE   = "/com/example/flappybird/Picture/message.png";
    public static final String GAMEOVER  = "/com/example/flappybird/Picture/TTgameOver.png";
    public static final String PAUSE_BTN = "/com/example/flappybird/Picture/pause.png";
    public static final String PAUSE_TAB = "/com/example/flappybird/Picture/pauseTab.png";
    public static final String RESUME    = "/com/example/flappybird/Picture/resume.png";
    public static final String REPLAY    = "/com/example/flappybird/Picture/replay.png";
    public static final String BUTTON    = "/com/example/flappybird/Picture/button.png";
    public static final String ARROW_L   = "/com/example/flappybird/Picture/nextLeft.png";
    public static final String ARROW_R   = "/com/example/flappybird/Picture/nextRight.png";
    public static final String SOUND_ON  = "/com/example/flappybird/Picture/soundon.png";
    public static final String SOUND_OFF = "/com/example/flappybird/Picture/soundoff.png";

    // Numbers (0-9) large and small
    public static String largeNum(int d) {
        return "/com/example/flappybird/Picture/BigNum/" + d + ".png";
    }
    public static String smallNum(int d) {
        return "/com/example/flappybird/Picture/SmallNum/" + d + ".png";
    }

    // Medals
    public static String medal(int idx) {
        return "/com/example/flappybird/Picture/medal/" + idx + ".png";
    }

    // Audio
    public static final String SFX_CLICK = "/com/example/flappybird/Sound/mouse-click.mp3";
    public static final String SFX_FLAP  = "/com/example/flappybird/Sound/flap.mp3";
    public static final String SFX_DEAD  = "/com/example/flappybird/Sound/dead.mp3";
    public static final String SFX_POINT = "/com/example/flappybird/Sound/point.wav";
    public static final String MUSIC     = "/com/example/flappybird/Sound/music.mp3";
}
