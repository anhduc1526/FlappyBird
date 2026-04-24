package com.example.flappybird.controller;

import com.example.flappybird.model.BirdModel;
import com.example.flappybird.util.GameConstants;
import com.example.flappybird.view.GameRenderer;

/**
 * CONTROLLER helper — State machine của death animation.
 *
 * Vấn đề cũ: 4 biến state (deathAnimActive, deathPhase, panelSlide,
 * deadInputReady) nằm trong GameController, làm class đó "fat".
 *
 * Giờ đây toàn bộ logic death animation được đóng gói ở đây.
 *
 * Lifecycle:
 *   begin()   → Phase.FALLING   (bird rơi xuống)
 *   advance() → Phase.SLIDING   (panel ease-out vào)
 *             → Phase.WAITING   (chờ input Replay)
 *   reset()   → trở về trạng thái ban đầu
 *
 * Design Pattern: State Machine / Strategy.
 */
public class DeathAnimController {

    // ── Internal phases ───────────────────────────────────────────────────────

    public enum Phase { IDLE, FALLING, SLIDING, WAITING }

    private Phase  phase         = Phase.IDLE;
    private double panelSlide    = 220.0;  // ease-out offset: 220 → 0
    private boolean inputReady   = false;

    // ── Constants ─────────────────────────────────────────────────────────────

    /** Bước xoay tối thiểu mỗi tick (độ). */
    private static final double ANGLE_STEP   = 6.0;
    /** Tốc độ ease-out cho panel slide (phần còn lại × hệ số). */
    private static final double SLIDE_FACTOR = 0.18;
    /** Bước slide tối thiểu để không bị stuck. */
    private static final double SLIDE_MIN    = 2.0;

    // ─────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────

    /** Gọi ngay khi GameModel chuyển sang Phase.DEAD. */
    public void begin(BirdModel bird) {
        phase      = Phase.FALLING;
        panelSlide = 220.0;
        inputReady = false;

        bird.setAngle(90);      // snap nose-down
        bird.resetDeathDy();
    }

    /**
     * Gọi mỗi tick khi phase == DEAD.
     * Tự vẽ scene chết (background/pipes/land/bird) và xử lý animation.
     *
     * @param renderer  GameRenderer hiện tại
     * @param bird      BirdModel (để trượt xuống và xoay)
     * @param score     điểm hiện tại
     * @param bestScore điểm cao nhất
     * @param medalIdx  chỉ số huy chương (-1 = không có)
     */
    public void advance(GameRenderer renderer, BirdModel bird,
                        int score, int bestScore, int medalIdx) {
        switch (phase) {
            case FALLING -> advanceFalling(bird);
            case SLIDING -> advanceSliding(renderer, score, bestScore, medalIdx);
            case WAITING -> renderer.drawGameOverPanel(score, bestScore, medalIdx, 0);
            default      -> { /* IDLE: không làm gì */ }
        }
    }

    /** True khi panel đã vào hẳn và người chơi có thể bấm Replay. */
    public boolean isInputReady() { return inputReady; }

    /** True khi animation đang chạy (không phải IDLE). */
    public boolean isActive() { return phase != Phase.IDLE; }

    /** Reset về trạng thái ban đầu (gọi sau khi resetGame()). */
    public void reset() {
        phase      = Phase.IDLE;
        panelSlide = 220.0;
        inputReady = false;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private phase logic
    // ─────────────────────────────────────────────────────────────────────────

    private void advanceFalling(BirdModel bird) {
        // Rơi xuống với gravity thực
        bird.slideDown();

        // Xoay từ từ về 90°
        double angle = bird.getAngle();
        if (angle < 90) bird.setAngle(Math.min(90, angle + ANGLE_STEP));

        // Khi đất thì chuyển sang slide panel
        if (bird.getY() >= GameConstants.LAND_Y - GameConstants.BIRD_HEIGHT - 2) {
            phase = Phase.SLIDING;
        }
    }

    private void advanceSliding(GameRenderer renderer,
                                int score, int bestScore, int medalIdx) {
        // Ease-out: step = max(MIN, remaining * factor) → nhanh lúc đầu, nhẹ lúc cuối
        double step = Math.max(SLIDE_MIN, panelSlide * SLIDE_FACTOR);
        panelSlide -= step;

        if (panelSlide <= 0) {
            panelSlide = 0;
            phase      = Phase.WAITING;
            inputReady = true;
        }

        renderer.drawGameOverPanel(score, bestScore, medalIdx, panelSlide);
    }
}