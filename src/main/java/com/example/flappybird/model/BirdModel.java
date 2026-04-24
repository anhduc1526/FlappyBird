package com.example.flappybird.model;

import com.example.flappybird.util.GameConstants;

/**
 * MODEL — Bird entity.
 * Design Pattern: Pure domain model (no JavaFX dependencies).
 */
public class BirdModel {

    // ── position ──────────────────────────────────────────────────────────────
    private double x = GameConstants.BIRD_INITIAL_X;
    private double y = GameConstants.BIRD_INITIAL_Y;

    // ── physics ───────────────────────────────────────────────────────────────
    private static final double GRAVITY    = 0.5;   // tăng từ 0.35 → cảm giác nặng hơn, rơi nhanh hơn
    private static final double MAX_FALL   = 12.0;  // tăng terminal velocity
    private static final double FLAP_SPEED = -9.0;  // tăng từ -7.0 → flap mạnh hơn

    private double dy     = 1.0;
    private double angle  = 0;
    private double deathDy = 2.0;

    // ── animation ─────────────────────────────────────────────────────────────
    private int indexAnimation = 0;

    // ── appearance ────────────────────────────────────────────────────────────
    private int typeBird = 0; // 0=yellow, 1=red, 2=blue

    // ─────────────────────────────────────────────────────────────────────────

    public void move() {
        dy += GRAVITY;
        if (dy > MAX_FALL) dy = MAX_FALL;

        y += dy;

        if (y <= 0) { y = 0; dy = 0; }
        if (y >= GameConstants.LAND_Y - GameConstants.BIRD_HEIGHT) {
            y = GameConstants.LAND_Y - GameConstants.BIRD_HEIGHT;
            dy = 0;
        }

        // Smooth angle lerp theo vận tốc
        double targetAngle;
        if (dy < -3) {
            targetAngle = -30;
        } else if (dy < 2) {
            targetAngle = 0;
        } else {
            targetAngle = Math.min(90, dy / MAX_FALL * 90);
        }
        double lerpSpeed = (targetAngle > angle) ? 7.0 : 5.0;
        angle += (targetAngle - angle) / lerpSpeed;
    }

    public void flapUp() {
        dy    = FLAP_SPEED;
        angle = -30;
    }

    public void turnDown() {}

    public void updateAnimation() {
        indexAnimation++;
    }

    public int getAnimationFrame() {
        return indexAnimation % GameConstants.ANIM_FRAME_COUNT;
    }

    public void slideDown() {
        deathDy += 0.6;
        if (deathDy > MAX_FALL) deathDy = MAX_FALL;
        y += deathDy;
    }

    public void resetDeathDy() { deathDy = 2.0; }

    public void changeTypeBird(int delta) {
        typeBird = ((typeBird + delta) % GameConstants.BIRD_TYPE_COUNT
                + GameConstants.BIRD_TYPE_COUNT) % GameConstants.BIRD_TYPE_COUNT;
    }

    public void reset() {
        x           = GameConstants.BIRD_INITIAL_X;
        y           = GameConstants.BIRD_INITIAL_Y;
        angle       = 0;
        dy          = 1.0;
        deathDy     = 2.0;
        indexAnimation = 0;
    }

    public double getX()        { return x; }
    public double getY()        { return y; }
    public double getAngle()    { return angle; }
    public int    getTypeBird() { return typeBird; }

    public void setAngle(double angle) { this.angle = angle; }
    public void setY(double y)         { this.y = y; }

    public double getRight()  { return x + GameConstants.BIRD_WIDTH; }
    public double getBottom() { return y + GameConstants.BIRD_HEIGHT; }
}