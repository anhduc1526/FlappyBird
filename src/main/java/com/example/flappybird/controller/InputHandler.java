package com.example.flappybird.controller;

import com.example.flappybird.model.GameModel;
import com.example.flappybird.model.GameModel.Phase;
import com.example.flappybird.util.GameConstants;
import com.example.flappybird.view.AudioManager;
import com.example.flappybird.view.HudLayout;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;

/**
 * CONTROLLER helper — Xử lý toàn bộ keyboard và mouse input.
 *
 * Vấn đề cũ: toàn bộ handleMenuClick(), handlePlayingClick(),
 * handleDeadClick(), handlePauseClick() nằm trong GameController,
 * cộng thêm attachInput() → class đó phình to ~250 dòng.
 *
 * InputHandler nhận 2 callback:
 *  - onFlap:       bird flap (phase PLAYING)
 *  - onDeadClick:  người chơi bấm Replay khi death panel hiện
 *
 * Design Pattern: Command / Handler.
 */
public class InputHandler {

    private final GameModel    model;
    private final AudioManager audio;

    /** Callback → GameController biết khi cần bắt đầu death anim hoặc replay. */
    private final Runnable onDeadReplayClick;

    public InputHandler(GameModel model, AudioManager audio, Runnable onDeadReplayClick) {
        this.model             = model;
        this.audio             = audio;
        this.onDeadReplayClick = onDeadReplayClick;
    }

    // ── Gắn vào Scene ────────────────────────────────────────────────────────

    /** Gắn tất cả event listeners vào scene. Gọi một lần trong constructor. */
    public void attach(Scene scene, DeathAnimController deathAnim) {
        scene.setOnKeyPressed(e  -> handleKey(e.getCode()));
        scene.setOnMouseClicked(e -> handleMouse(e, deathAnim));
    }

    // ── Keyboard ──────────────────────────────────────────────────────────────

    private void handleKey(KeyCode code) {
        switch (code) {
            case SPACE  -> handleFlap();
            case UP     -> handleFlap();
            case P      -> model.togglePause();
            case M      -> handleToggleSound();
            case ESCAPE -> model.requestQuit();
        }
    }

    // ── Mouse ─────────────────────────────────────────────────────────────────

    private void handleMouse(MouseEvent e, DeathAnimController deathAnim) {
        double mx = e.getX();
        double my = e.getY();
        Phase  p  = model.getPhase();

        switch (p) {
            case MENU    -> handleMenuClick(mx, my);
            case PLAYING -> handlePlayingClick(mx, my);
            case DEAD    -> { if (deathAnim.isInputReady()) handleDeadClick(mx, my); }
            case PAUSED  -> handlePauseClick(mx, my);
        }
    }

    // ── Per-phase click handlers ───────────────────────────────────────────────

    private void handleMenuClick(double mx, double my) {
        if (inBox(mx, my, HudLayout.SOUND_MENU_X, HudLayout.SOUND_MENU_Y,
                HudLayout.SOUND_W, HudLayout.SOUND_H)) {
            handleToggleSound();

        } else if (inBox(mx, my, HudLayout.ARROW_LEFT_X, HudLayout.ARROW_LEFT_Y,
                HudLayout.ARROW_W, HudLayout.ARROW_H)) {
            model.changeBirdType(-1);
            playClick();

        } else if (inBox(mx, my, HudLayout.ARROW_RIGHT_X, HudLayout.ARROW_RIGHT_Y,
                HudLayout.ARROW_W, HudLayout.ARROW_H)) {
            model.changeBirdType(1);
            playClick();

        } else if (inBox(mx, my, HudLayout.DAY_ARROW_L_X, HudLayout.DAY_ARROW_Y,
                HudLayout.ARROW_W, HudLayout.ARROW_H)
                || inBox(mx, my, HudLayout.DAY_ARROW_R_X, HudLayout.DAY_ARROW_Y,
                HudLayout.ARROW_W, HudLayout.ARROW_H)) {
            model.toggleDay();
            playClick();

        } else {
            // Bấm bất kỳ chỗ nào khác → bắt đầu game
            model.startGame();
        }
    }

    private void handlePlayingClick(double mx, double my) {
        if (inBox(mx, my, HudLayout.PAUSE_BTN_X, HudLayout.PAUSE_BTN_Y,
                HudLayout.PAUSE_BTN_W, HudLayout.PAUSE_BTN_H)) {
            playClick();
            model.pauseGame();
        } else {
            handleFlap();
        }
    }

    private void handleDeadClick(double mx, double my) {
        if (inBox(mx, my, HudLayout.REPLAY_X, HudLayout.REPLAY_Y,
                HudLayout.REPLAY_W, HudLayout.REPLAY_H)) {
            playClick();
            onDeadReplayClick.run();
        }
    }

    private void handlePauseClick(double mx, double my) {
        if (inBox(mx, my, HudLayout.PAUSE_SOUND_X, HudLayout.PAUSE_SOUND_Y,
                HudLayout.SOUND_W, HudLayout.SOUND_H)) {
            handleToggleSound();

        } else if (inBox(mx, my, HudLayout.PAUSE_DAY_BTN_X, HudLayout.PAUSE_DAY_BTN_Y,
                HudLayout.PAUSE_DAY_BTN_W, HudLayout.PAUSE_DAY_BTN_H)) {
            model.toggleDay();
            playClick();

        } else if (inBox(mx, my, HudLayout.PAUSE_RESUME_X, HudLayout.PAUSE_RESUME_Y,
                HudLayout.PAUSE_RESUME_W, HudLayout.PAUSE_RESUME_H)) {
            playClick();
            model.resumeGame();
        }
    }

    // ── Shared helpers ────────────────────────────────────────────────────────

    private void handleFlap() {
        Phase p = model.getPhase();
        if (p == Phase.MENU) {
            model.startGame();
        } else if (p == Phase.PLAYING) {
            model.birdFlap();
            audio.playFlap();
        }
    }

    private void handleToggleSound() {
        model.toggleSound();
        // AudioManager phản ứng qua GameEvent.SOUND_TOGGLED (đăng ký trong GameController)
        playClick();
    }

    private void playClick() {
        audio.playClick();
    }

    /** Hit-test hình chữ nhật. */
    private static boolean inBox(double mx, double my,
                                 double x, double y, double w, double h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}