package com.example.flappybird.util;

/**
 * Game-wide constants.
 * Ported from def.h in the original C++ project.
 */
public final class GameConstants {
    private GameConstants() {}

    // Window
    public static final String WINDOW_TITLE = "Flappy Bird";
    public static final int SCREEN_WIDTH  = 350;
    public static final int SCREEN_HEIGHT = 625;

    // Bird
    public static final int BIRD_WIDTH  = 34;
    public static final int BIRD_HEIGHT = 24;

    // Pipe
    public static final int PIPE_SPACE    = 220;   // vertical gap between upper/lower pipes
    public static final int PIPE_DISTANCE = 220;   // horizontal distance between pipes
    public static final int PIPE_WIDTH    = 52;
    public static final int PIPE_HEIGHT   = 320;

    // Land
    public static final int LAND_HEIGHT = 140;
    public static final int LAND_Y      = 500;     // y at which land is rendered

    // HUD overlay textures
    public static final int MESSAGE_WIDTH  = 184;
    public static final int MESSAGE_HEIGHT = 267;
    public static final int GAMEOVER_WIDTH  = 250;
    public static final int GAMEOVER_HEIGHT = 204;

    // Physics (from Bird logic in C++)
    public static final int BIRD_FLAP_SPEED   = -5;  // dy when spacebar pressed (du)
    public static final int BIRD_MAX_FALL      = 12;
    public static final int BIRD_INITIAL_X     = SCREEN_WIDTH / 7;
    public static final int BIRD_INITIAL_Y     = SCREEN_HEIGHT / 2;

    // Animation
    public static final int ANIM_FRAME_COUNT  = 48; // total animation frames cycle
    public static final int ANIM_UP_END       = 17;
    public static final int ANIM_MID_END      = 33;

    // Scroll speed
    public static final int SCROLL_SPEED = 2;

    // Pipe generation
    public static final int PIPE_COUNT    = 4;
    public static final int PIPE_START_X  = 350;
    public static final int PIPE_RESET_X  = 500;
    public static final int PIPE_Y_RANGE  = 141;    // rand() % 141

    // Pipe offsets matching original C++ setPosY logic:
    //   upper.y = -320 + 100 + posY  →  -220 + posY
    //   under.y = posY + 270
    public static final int PIPE_UPPER_BASE = -220;
    public static final int PIPE_LOWER_BASE = 270;

    // Score thresholds for medals
    public static final int MEDAL_BRONZE_MIN = 10;
    public static final int MEDAL_SILVER_MIN = 20;
    public static final int MEDAL_GOLD_MIN   = 30;

    // FPS target
    public static final int TARGET_FPS = 60;
    public static final double FRAME_DURATION_MS = 1000.0 / TARGET_FPS;

    // Score file
    public static final String SCORE_FILE = "score.txt";

    // Number of bird types (yellow / red / blue)
    public static final int BIRD_TYPE_COUNT = 3;
}
