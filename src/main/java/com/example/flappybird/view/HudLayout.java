package com.example.flappybird.view;

import com.example.flappybird.util.GameConstants;

/**
 * VIEW helper — Tập trung toàn bộ toạ độ HUD.
 *
 * Vấn đề cũ: magic numbers nằm rải rác trong cả GameRenderer lẫn GameController
 * (ví dụ inBox(mx, my, 105, 266, 32, 24) — không ai biết 105/266 là gì).
 *
 * Giờ đây mọi toạ độ đều có tên rõ ràng, dùng chung ở cả render và hit-test.
 *
 * Design Pattern: Constants holder (utility class, không instantiate).
 */
public final class HudLayout {

    private HudLayout() {}

    // ── Sound icon (menu + pause) ─────────────────────────────────────────────
    public static final int SOUND_MENU_X  = 10;
    public static final int SOUND_MENU_Y  = 10;
    public static final int SOUND_W       = 32;
    public static final int SOUND_H       = 24;

    // ── Bird selector arrows (menu) ───────────────────────────────────────────
    public static final int ARROW_LEFT_X  = 31;
    public static final int ARROW_LEFT_Y  = 317;
    public static final int ARROW_RIGHT_X = 90;
    public static final int ARROW_RIGHT_Y = 317;
    public static final int ARROW_W       = 13;
    public static final int ARROW_H       = 16;

    // ── Day/night toggle arrows (menu sides) ──────────────────────────────────
    public static final int DAY_ARROW_L_X = 0;
    public static final int DAY_ARROW_R_X = GameConstants.SCREEN_WIDTH - ARROW_W;
    public static final int DAY_ARROW_Y   = GameConstants.SCREEN_HEIGHT / 2;

    // ── Pause button (in-game top-right) ──────────────────────────────────────
    public static final int PAUSE_BTN_X = 320;
    public static final int PAUSE_BTN_Y = 10;
    public static final int PAUSE_BTN_W = 26;
    public static final int PAUSE_BTN_H = 28;

    // ── Score (in-game large digits, centered) ────────────────────────────────
    public static final int LARGE_SCORE_DIGIT_W = 30;
    public static final int LARGE_SCORE_Y        = 10;

    // ── Game-over panel ───────────────────────────────────────────────────────
    public static final int GAME_OVER_X =
            (GameConstants.SCREEN_WIDTH - GameConstants.GAMEOVER_WIDTH) / 2;
    public static final int GAME_OVER_Y =
            (500 - GameConstants.GAMEOVER_HEIGHT) / 2;

    public static final int MEDAL_X           = 75;
    public static final int MEDAL_Y           = 263;
    public static final int GO_SCORE_ROW_Y    = 263;
    public static final int GO_BEST_ROW_Y     = 313;

    public static final int REPLAY_X = (GameConstants.SCREEN_WIDTH - 100) / 2;
    public static final int REPLAY_Y = 360;
    public static final int REPLAY_W = 100;
    public static final int REPLAY_H = 56;

    // ── Small score (right-aligned inside panel) ──────────────────────────────
    public static final int SMALL_SCORE_DIGIT_W  = 21;
    public static final int SMALL_SCORE_RIGHT_X  = 271; // anchor x (right edge of digit string)

    // ── Pause overlay ─────────────────────────────────────────────────────────
    public static final int PAUSE_TAB_W = 250;
    public static final int PAUSE_TAB_X = (GameConstants.SCREEN_WIDTH - PAUSE_TAB_W) / 2;
    public static final int PAUSE_TAB_Y =
            (GameConstants.SCREEN_HEIGHT - 128) / 2 - 17;

    public static final int PAUSE_SOUND_X = 105;
    public static final int PAUSE_SOUND_Y = 266;

    public static final int PAUSE_DAY_BTN_X = 105;
    public static final int PAUSE_DAY_BTN_Y = 316;
    public static final int PAUSE_DAY_BTN_W = 26;
    public static final int PAUSE_DAY_BTN_H = 26;

    public static final int PAUSE_RESUME_X = (GameConstants.SCREEN_WIDTH - 26) / 2;
    public static final int PAUSE_RESUME_Y = 365;
    public static final int PAUSE_RESUME_W = 26;
    public static final int PAUSE_RESUME_H = 28;

    public static final int PAUSE_SCORE_ROW_Y    = 263;
    public static final int PAUSE_BEST_ROW_Y     = 313;
}