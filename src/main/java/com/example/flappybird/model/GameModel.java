package com.example.flappybird.model;

import com.example.flappybird.util.GameConstants;
import com.example.flappybird.util.ScoreManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * MODEL — Central game state.
 *
 * Owns all sub-models: bird, pipes, backgrounds, score.
 * Contains all game-logic methods (collision, scoring, pipe recycling).
 * No JavaFX / rendering dependencies whatsoever.
 *
 * Design Pattern: Facade over sub-models; Observer-ready (controllers poll).
 */
public class GameModel {

    // ── Sub-models ────────────────────────────────────────────────────────────
    private final BirdModel bird = new BirdModel();
    private final List<PipeModel> pipes = new ArrayList<>();
    private PipeModel templatePipe;

    // 288 px wide is the standard Flappy Bird background width
    private final ScrollingBackground dayBg    = new ScrollingBackground(288);
    private final ScrollingBackground nightBg  = new ScrollingBackground(288);
    private final ScrollingBackground land      = new ScrollingBackground(336);

    // ── Game state ────────────────────────────────────────────────────────────
    public enum Phase { MENU, PLAYING, DEAD, PAUSED }

    private Phase phase = Phase.MENU;
    private int   score     = 0;
    private int   bestScore = 0;
    private boolean dayMode  = true;
    private boolean soundOn  = true;
    private boolean quit     = false;

    private final Random rng = new Random();

    // ── Constructor ───────────────────────────────────────────────────────────

    public GameModel() {
        bestScore = ScoreManager.loadBestScore();
        initPipes(GameConstants.PIPE_START_X);
    }

    // ── Pipe helpers ──────────────────────────────────────────────────────────

    private void initPipes(int startX) {
        pipes.clear();
        for (int i = 0; i <= GameConstants.PIPE_COUNT - 1; i++) {
            pipes.add(new PipeModel(
                    startX + GameConstants.PIPE_DISTANCE * i,
                    rng.nextInt(GameConstants.PIPE_Y_RANGE)));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Game-tick update — called by the controller at 60 fps
    // ─────────────────────────────────────────────────────────────────────────

    /** Advance one game tick (called only when phase == PLAYING). */
    public void tick() {
        bird.move();

        // Scroll backgrounds & land
        dayBg.scroll(GameConstants.SCROLL_SPEED);
        nightBg.scroll(GameConstants.SCROLL_SPEED);
        land.scroll(GameConstants.SCROLL_SPEED);

        // Scroll pipes + score + recycle
        for (PipeModel p : pipes) p.scroll(GameConstants.SCROLL_SPEED);

        // Recycle off-screen pipe (mirrors game.cpp updateG logic)
        if (pipes.get(0).isOffScreen()) {
            pipes.remove(0);
            double newX = pipes.get(pipes.size() - 1).getX() + GameConstants.PIPE_DISTANCE;
            pipes.add(new PipeModel(newX, rng.nextInt(GameConstants.PIPE_Y_RANGE)));
        }

        // Score — mirrors Game::updateScore
        if (pipes.get(0).birdJustPassed(bird)) {
            score++;
        }

        // Collision — mirrors Game::checkCollide
        boolean pipeHit = pipes.get(0).collidesWith(bird);
        boolean floorHit = bird.getY() >= GameConstants.LAND_Y - GameConstants.BIRD_HEIGHT;
        if (pipeHit || floorHit) {
            if (score > bestScore) {
                bestScore = score;
                ScoreManager.saveBestScore(bestScore);
            }
            phase = Phase.DEAD;
        }
    }

    /** Tick used only during the menu idle animation (scroll backgrounds, bob bird). */
    public void menuTick() {
        dayBg.scroll(GameConstants.SCROLL_SPEED);
        nightBg.scroll(GameConstants.SCROLL_SPEED);
        land.scroll(GameConstants.SCROLL_SPEED);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // State transitions
    // ─────────────────────────────────────────────────────────────────────────

    public void startGame() {
        phase = Phase.PLAYING;
    }

    public void pauseGame() {
        if (phase == Phase.PLAYING) phase = Phase.PAUSED;
    }

    public void resumeGame() {
        if (phase == Phase.PAUSED) phase = Phase.PLAYING;
    }

    public void resetGame() {
        bird.reset();
        score = 0;
        initPipes(GameConstants.PIPE_RESET_X);
        phase = Phase.MENU;
    }

    public void requestQuit() {
        quit = true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Input delegation
    // ─────────────────────────────────────────────────────────────────────────

    /** Bird flap (SPACE or click during PLAYING). */
    public void birdFlap() {
        if (phase == Phase.PLAYING) bird.flapUp();
    }

    /** Begin passive fall (key/button released). */
    public void birdTurnDown() {
        if (phase == Phase.PLAYING) bird.turnDown();
    }

    public void changeBirdType(int delta) { bird.changeTypeBird(delta); }
    public void toggleDay()               { dayMode = !dayMode; }
    public void toggleSound()             { soundOn = !soundOn; }

    // ─────────────────────────────────────────────────────────────────────────
    // Getters — read-only access for View / Controller
    // ─────────────────────────────────────────────────────────────────────────

    public BirdModel              getBird()      { return bird; }
    public List<PipeModel>        getPipes()     { return pipes; }
    public ScrollingBackground    getDayBg()     { return dayBg; }
    public ScrollingBackground    getNightBg()   { return nightBg; }
    public ScrollingBackground    getLand()      { return land; }
    public Phase                  getPhase()     { return phase; }
    public int                    getScore()     { return score; }
    public int                    getBestScore() { return bestScore; }
    public boolean                isDayMode()    { return dayMode; }
    public boolean                isSoundOn()    { return soundOn; }
    public boolean                isQuit()       { return quit; }

    /** Medal index: 0=bronze, 1=silver, 2=gold; -1=none. */
    public int getMedalIndex() {
        if (score >= GameConstants.MEDAL_GOLD_MIN)   return 2;
        if (score >= GameConstants.MEDAL_SILVER_MIN) return 1;
        if (score >= GameConstants.MEDAL_BRONZE_MIN) return 0;
        return -1;
    }
}
