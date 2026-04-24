package com.example.flappybird.controller;

import com.example.flappybird.model.GameModel;
import com.example.flappybird.model.GameModel.Phase;
import com.example.flappybird.util.GameConstants;
import com.example.flappybird.view.AudioManager;
import com.example.flappybird.view.GameRenderer;
import com.example.flappybird.view.GameView;
import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * CONTROLLER — Wires model, view, and input together.
 * Design Pattern: MVC Controller + Game Loop (AnimationTimer).
 */
public class GameController {

    private final GameModel    model;
    private final GameView     view;
    private final GameRenderer renderer;
    private final AudioManager audio = AudioManager.getInstance();

    // ── Death animation state ─────────────────────────────────────────────────
    private boolean deathAnimActive = false;
    // phase: 0 = bird tumbling down   1 = panel sliding in   2 = results (wait input)
    private int     deathPhase     = 0;
    private double  panelSlide     = 220.0;  // ease-out offset: 220 → 0
    private boolean deadInputReady = false;

    // ── Frame timing ─────────────────────────────────────────────────────────
    private long lastNanos = 0;

    // ── Main loop ─────────────────────────────────────────────────────────────
    private AnimationTimer loop;

    public GameController(Stage stage) {
        model    = new GameModel();
        view     = new GameView(stage);
        renderer = view.getRenderer();

        audio.loadAll();
        if (model.isSoundOn()) audio.playMusic();

        attachInput();
        startLoop();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Game loop
    // ─────────────────────────────────────────────────────────────────────────

    private void startLoop() {
        loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastNanos == 0) { lastNanos = now; return; }
                double elapsedMs = (now - lastNanos) / 1_000_000.0;
                if (elapsedMs < GameConstants.FRAME_DURATION_MS) return;
                lastNanos = now;
                tick();
            }
        };
        loop.start();
    }

    private void tick() {
        renderer.clear();
        Phase phase = model.getPhase();

        switch (phase) {
            case MENU    -> tickMenu();
            case PLAYING -> tickPlaying();
            case DEAD    -> tickDead();
            case PAUSED  -> tickPaused();
        }

        if (model.isQuit()) {
            loop.stop();
            audio.dispose();
            System.exit(0);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Per-phase render + logic
    // ─────────────────────────────────────────────────────────────────────────

    private void tickMenu() {
        model.menuTick();
        renderer.drawBackground(
                model.isDayMode() ? model.getDayBg() : model.getNightBg(),
                model.isDayMode());
        renderer.drawLand(model.getLand());
        renderer.drawBird(model.getBird());
        renderer.drawMenu(model.isSoundOn());
    }

    private void tickPlaying() {
        model.tick();

        // If model just transitioned to DEAD, start death animation
        if (model.getPhase() == Phase.DEAD && !deathAnimActive) {
            beginDeathAnimation();
            return;
        }

        renderer.drawBackground(
                model.isDayMode() ? model.getDayBg() : model.getNightBg(),
                model.isDayMode());
        renderer.drawPipes(model.getPipes());
        renderer.drawLand(model.getLand());
        renderer.drawBird(model.getBird());
        renderer.drawLargeScore(model.getScore());
        renderer.drawPauseButton();
    }

    private void tickDead() {
        if (!deathAnimActive) {
            beginDeathAnimation();
            return;
        }
        advanceDeathAnimation();
    }

    private void tickPaused() {
        renderer.drawBackground(
                model.isDayMode() ? model.getDayBg() : model.getNightBg(),
                model.isDayMode());
        renderer.drawPipes(model.getPipes());
        renderer.drawLand(model.getLand());
        renderer.drawBird(model.getBird());
        renderer.drawLargeScore(model.getScore());
        renderer.drawPauseOverlay(model.getScore(), model.getBestScore(), model.isSoundOn());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Death animation
    // ─────────────────────────────────────────────────────────────────────────

    private void beginDeathAnimation() {
        deathAnimActive = true;
        deathPhase      = 0;
        panelSlide      = 220.0;
        deadInputReady  = false;
        model.getBird().setAngle(90);    // snap nose-down on impact
        model.getBird().resetDeathDy();
        if (model.isSoundOn()) audio.playDead();
    }

    private void advanceDeathAnimation() {
        drawDeadScene();

        switch (deathPhase) {
            case 0 -> {
                // Bird falls down with real gravity + rotates nose-down to 90°
                model.getBird().slideDown();
                double a = model.getBird().getAngle();
                if (a < 90) model.getBird().setAngle(Math.min(90, a + 6));

                // Transition to panel slide when bird hits the ground
                if (model.getBird().getY() >= GameConstants.LAND_Y - GameConstants.BIRD_HEIGHT - 2) {
                    deathPhase = 1;
                }
            }
            case 1 -> {
                // Ease-out slide: step shrinks as panel approaches target
                // Formula: step = max(2, remaining * 0.18)  →  fast start, soft landing
                double step = Math.max(2.0, panelSlide * 0.18);
                panelSlide -= step;
                if (panelSlide <= 0) {
                    panelSlide     = 0;
                    deathPhase     = 2;
                    deadInputReady = true;
                }
                renderer.drawGameOverPanel(model.getScore(), model.getBestScore(),
                        model.getMedalIndex(), panelSlide);
            }
            case 2 -> {
                // Fully shown — wait for replay click
                renderer.drawGameOverPanel(model.getScore(), model.getBestScore(),
                        model.getMedalIndex(), 0);
            }
        }
    }

    private void drawDeadScene() {
        renderer.drawBackground(
                model.isDayMode() ? model.getDayBg() : model.getNightBg(),
                model.isDayMode());
        renderer.drawPipes(model.getPipes());
        renderer.drawLand(model.getLand());
        renderer.drawBird(model.getBird());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Input
    // ─────────────────────────────────────────────────────────────────────────

    private void attachInput() {
        view.getScene().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE)  handleFlap();
            if (e.getCode() == KeyCode.P)      handlePause();
            if (e.getCode() == KeyCode.M)      handleToggleSound();
            if (e.getCode() == KeyCode.ESCAPE) model.requestQuit();
        });

        view.getScene().setOnMouseClicked(this::handleMouseClick);
    }

    private void handleFlap() {
        Phase p = model.getPhase();
        if (p == Phase.MENU) {
            model.startGame();
        } else if (p == Phase.PLAYING) {
            model.birdFlap();
            if (model.isSoundOn()) audio.playFlap();
        }
    }

    private void handlePause() {
        if (model.getPhase() == Phase.PLAYING) model.pauseGame();
        else if (model.getPhase() == Phase.PAUSED) model.resumeGame();
    }

    private void handleToggleSound() {
        model.toggleSound();
        audio.setSoundEnabled(model.isSoundOn());
    }

    private void handleMouseClick(MouseEvent e) {
        double mx = e.getX();
        double my = e.getY();
        Phase phase = model.getPhase();

        if      (phase == Phase.MENU)                     handleMenuClick(mx, my);
        else if (phase == Phase.PLAYING)                  handlePlayingClick(mx, my);
        else if (phase == Phase.DEAD && deadInputReady)   handleDeadClick(mx, my);
        else if (phase == Phase.PAUSED)                   handlePauseClick(mx, my);
    }

    private void handleMenuClick(double mx, double my) {
        if (inBox(mx, my, 10, 10, 32, 24)) {
            handleToggleSound();
            if (model.isSoundOn()) audio.playClick();
        } else if (inBox(mx, my, 31, 317, 13, 16)) {
            model.changeBirdType(-1);
            if (model.isSoundOn()) audio.playClick();
        } else if (inBox(mx, my, 90, 317, 13, 16)) {
            model.changeBirdType(1);
            if (model.isSoundOn()) audio.playClick();
        } else if (inBox(mx, my, 0, GameConstants.SCREEN_HEIGHT / 2.0, 13, 16)) {
            model.toggleDay();
            if (model.isSoundOn()) audio.playClick();
        } else if (inBox(mx, my, GameConstants.SCREEN_WIDTH - 13, GameConstants.SCREEN_HEIGHT / 2.0, 13, 16)) {
            model.toggleDay();
            if (model.isSoundOn()) audio.playClick();
        } else {
            model.startGame();
        }
    }

    private void handlePlayingClick(double mx, double my) {
        if (inBox(mx, my, 320, 10, 26, 28)) {
            if (model.isSoundOn()) audio.playClick();
            model.pauseGame();
        } else {
            model.birdFlap();
            if (model.isSoundOn()) audio.playFlap();
        }
    }

    private void handleDeadClick(double mx, double my) {
        if (inBox(mx, my, 125, 360, 100, 56)) {
            if (model.isSoundOn()) audio.playClick();
            model.resetGame();
            deathAnimActive = false;
            deathPhase      = 0;
        }
    }

    private void handlePauseClick(double mx, double my) {
        if (inBox(mx, my, 105, 266, 32, 24)) {
            handleToggleSound();
            if (model.isSoundOn()) audio.playClick();
        } else if (inBox(mx, my, 105, 316, 26, 26)) {
            model.toggleDay();
            if (model.isSoundOn()) audio.playClick();
        } else if (inBox(mx, my, 162, 365, 26, 28)) {
            if (model.isSoundOn()) audio.playClick();
            model.resumeGame();
        }
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private boolean inBox(double mx, double my, double x, double y, double w, double h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}