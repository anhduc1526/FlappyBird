package com.example.flappybird.model;

import com.example.flappybird.util.GameConstants;

/**
 * MODEL — One pipe pair (upper + lower pipe).
 *
 * Mirrors the PIPE class from pipe.h / pipe.cpp.
 * Coordinates:
 *   upperY = PIPE_UPPER_BASE + posY   (top of upper pipe texture)
 *   lowerY = PIPE_LOWER_BASE + posY   (top of lower pipe texture)
 *
 * Design Pattern: Value Object / Entity model with no rendering logic.
 */
public class PipeModel {

    private double x;
    private double upperY;   // y of upper (downward-facing) pipe sprite
    private double lowerY;   // y of lower (upward-facing) pipe sprite

    public PipeModel(double x, int posY) {
        this.x = x;
        setPositionY(posY);
    }

    // ── Physics ───────────────────────────────────────────────────────────────

    /** Scroll left each tick. Mirrors PIPE::scroll. */
    public void scroll(int distance) {
        x -= distance;
    }

    /** Death animation: pipes fly apart. Mirrors PIPE::disY. */
    public void animateDeath() {
        upperY -= 5;
        lowerY += 5;
    }

    // ── Position helpers ─────────────────────────────────────────────────────

    public void setPositionX(double x)  { this.x = x; }

    public void setPositionY(int posY) {
        upperY = GameConstants.PIPE_UPPER_BASE + posY;
        lowerY = GameConstants.PIPE_LOWER_BASE + posY;
    }

    /** True once the pipe has fully scrolled off the left edge. */
    public boolean isOffScreen() {
        return x + GameConstants.PIPE_WIDTH <= 0;
    }

    // ── Collision detection ───────────────────────────────────────────────────
    // Mirrors Game::checkCollide logic from game.cpp

    public boolean collidesWith(BirdModel bird) {
        double bx = bird.getX();
        double by = bird.getY();
        double bRight  = bird.getRight();
        double bBottom = bird.getBottom();

        // Upper pipe collision
        if (bx <= x + GameConstants.PIPE_WIDTH && bRight >= x
                && by <= upperY + GameConstants.PIPE_HEIGHT
                && bBottom >= upperY) {
            return true;
        }
        // Lower pipe collision
        if (bx <= x + GameConstants.PIPE_WIDTH && bRight >= x
                && by <= lowerY + GameConstants.PIPE_HEIGHT
                && bBottom >= lowerY) {
            return true;
        }
        return false;
    }

    /** Mirrors Game::updateScore: bird has just passed this pipe. */
    public boolean birdJustPassed(BirdModel bird) {
        return (int) bird.getX() == (int) x;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public double getX()      { return x; }
    public double getUpperY() { return upperY; }
    public double getLowerY() { return lowerY; }
}
