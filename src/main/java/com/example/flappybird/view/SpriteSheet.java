package com.example.flappybird.view;

public final class SpriteSheet {
    private SpriteSheet() {}

    // Bird sprites  (3 types × 3 frames)
    public static final String[] BIRD_UP = {
            "/com/example/flappybird/img/yellowbird-upflap.png",
            "/com/example/flappybird/img/redbird-upflap.png",
            "/com/example/flappybird/img/bluebird-upflap.png"
    };
    public static final String[] BIRD_MID = {
            "/com/example/flappybird/img/yellowbird-midflap.png",
            "/com/example/flappybird/img/redbird-midflap.png",
            "/com/example/flappybird/img/bluebird-midflap.png"
    };
    public static final String[] BIRD_DOWN = {
            "/com/example/flappybird/img/yellowbird-downflap.png",
            "/com/example/flappybird/img/redbird-downflap.png",
            "/com/example/flappybird/img/bluebird-downflap.png"
    };

    // Pipes
    public static final String PIPE_UPPER = "/com/example/flappybird/img/pipe_upper.png";
    public static final String PIPE_LOWER = "/com/example/flappybird/img/pipe_under.png";

    // Backgrounds & land
    public static final String BG_DAY   = "/com/example/flappybird/img/background-day.png";
    public static final String BG_NIGHT = "/com/example/flappybird/img/background-night.png";
    public static final String LAND     = "/com/example/flappybird/img/land.png";

    // HUD
    public static final String MESSAGE   = "/com/example/flappybird/img/message.png";
    public static final String GAMEOVER  = "/com/example/flappybird/img/TTgameOver.png";
    public static final String PAUSE_BTN = "/com/example/flappybird/img/pause.png";
    public static final String PAUSE_TAB = "/com/example/flappybird/img/pauseTab.png";
    public static final String RESUME    = "/com/example/flappybird/img/resume.png";
    public static final String REPLAY    = "/com/example/flappybird/img/replay.png";
    public static final String BUTTON    = "/com/example/flappybird/img/button.png";
    public static final String ARROW_L   = "/com/example/flappybird/img/nextLeft.png";
    public static final String ARROW_R   = "/com/example/flappybird/img/nextRight.png";
    public static final String SOUND_ON  = "/com/example/flappybird/img/soundon.png";
    public static final String SOUND_OFF = "/com/example/flappybird/img/soundoff.png";

    // Numbers (0-9) large and small
    public static String largeNum(int d) {
        return "/com/example/flappybird/img/BigNum/" + d + ".png";
    }
    public static String smallNum(int d) {
        return "/com/example/flappybird/img/SmallNum/" + d + ".png";
    }

    // Medals
    public static String medal(int idx) {
        return "/com/example/flappybird/img/medal/" + idx + ".png";
    }

    // Audio
    public static final String SFX_CLICK = "/com/example/flappybird/Sound/mouse-click.mp3";
    public static final String SFX_FLAP  = "/com/example/flappybird/Sound/flap.mp3";
    public static final String SFX_DEAD  = "/com/example/flappybird/Sound/dead.mp3";
    public static final String SFX_POINT = "/com/example/flappybird/Sound/point.wav";
    public static final String MUSIC     = "/com/example/flappybird/Sound/music.mp3";
}
