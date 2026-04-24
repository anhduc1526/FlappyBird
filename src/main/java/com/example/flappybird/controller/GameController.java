package com.example.flappybird.controller;

import com.example.flappybird.model.GameEvent;
import com.example.flappybird.model.GameModel;
import com.example.flappybird.model.GameModel.Phase;
import com.example.flappybird.util.GameConstants;
import com.example.flappybird.view.AudioManager;
import com.example.flappybird.view.GameRenderer;
import com.example.flappybird.view.GameView;
import javafx.animation.AnimationTimer;
import javafx.stage.Stage;

/**
 * CONTROLLER — Điều phối model, view, input và game loop.
 *
 * Thay đổi so với phiên bản cũ:
 *  - Logic death animation → DeathAnimController
 *  - Logic input → InputHandler
 *  - Toạ độ HUD → HudLayout
 *  - String paths → Sprite enum
 *  - Observer callbacks thay thế phần poll model.isQuit()
 *
 * GameController giờ chỉ còn ~100 dòng, đúng vai trò "thin orchestrator".
 *
 * Design Pattern: MVC Controller + Game Loop (AnimationTimer).
 */
public class GameController {

    private final GameModel          model;
    private final GameView           view;
    private final GameRenderer       renderer;
    private final AudioManager       audio    = AudioManager.getInstance();
    private final DeathAnimController deathAnim = new DeathAnimController();
    private final InputHandler       input;

    private AnimationTimer loop;
    private long           lastNanos = 0;

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor — khởi tạo toàn bộ MVC triad
    // ─────────────────────────────────────────────────────────────────────────

    public GameController(Stage stage) {
        model    = new GameModel();
        view     = new GameView(stage);
        renderer = view.getRenderer();

        // InputHandler cần biết khi nào người dùng bấm Replay
        input = new InputHandler(model, audio, this::handleReplay);

        audio.loadAll();

        registerModelEvents();
        input.attach(view.getScene(), deathAnim);
        startLoop();

        if (model.isSoundOn()) audio.playMusic();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Observer registration
    // ─────────────────────────────────────────────────────────────────────────

    private void registerModelEvents() {
        model.on(GameEvent.BIRD_DIED,      e -> deathAnim.begin(model.getBird()));
        model.on(GameEvent.BIRD_DIED,      e -> { if (model.isSoundOn()) audio.playDead(); });
        model.on(GameEvent.SCORE_UPDATED,  e -> { if (model.isSoundOn()) audio.playPoint(); });
        model.on(GameEvent.SOUND_TOGGLED,  e -> audio.setSoundEnabled(model.isSoundOn()));
        model.on(GameEvent.QUIT_REQUESTED, e -> handleQuit());
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

        switch (model.getPhase()) {
            case MENU    -> tickMenu();
            case PLAYING -> tickPlaying();
            case DEAD    -> tickDead();
            case PAUSED  -> tickPaused();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Per-phase render
    // ─────────────────────────────────────────────────────────────────────────

    private void tickMenu() {
        model.menuTick();
        drawBackground();
        renderer.drawLand(model.getLand());
        renderer.drawBird(model.getBird());
        renderer.drawMenu(model.isSoundOn());
    }

    private void tickPlaying() {
        model.tick();
        // model.tick() có thể fire BIRD_DIED → deathAnim.begin() đã gọi
        // Nếu phase đã chuyển sang DEAD, tick tiếp theo sẽ xử lý tickDead()
        if (model.getPhase() == Phase.DEAD) return;

        drawBackground();
        renderer.drawPipes(model.getPipes());
        renderer.drawLand(model.getLand());
        renderer.drawBird(model.getBird());
        renderer.drawLargeScore(model.getScore());
        renderer.drawPauseButton();
    }

    private void tickDead() {
        // Vẽ scene tĩnh (nền + pipes + đất + bird)
        drawBackground();
        renderer.drawPipes(model.getPipes());
        renderer.drawLand(model.getLand());
        renderer.drawBird(model.getBird());

        // DeathAnimController xử lý phần còn lại (trượt panel, hiện replay)
        deathAnim.advance(renderer, model.getBird(),
                model.getScore(), model.getBestScore(), model.getMedalIndex());
    }

    private void tickPaused() {
        drawBackground();
        renderer.drawPipes(model.getPipes());
        renderer.drawLand(model.getLand());
        renderer.drawBird(model.getBird());
        renderer.drawLargeScore(model.getScore());
        renderer.drawPauseOverlay(model.getScore(), model.getBestScore(), model.isSoundOn());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Vẽ nền ngày hoặc đêm tuỳ dayMode. */
    private void drawBackground() {
        renderer.drawBackground(
                model.isDayMode() ? model.getDayBg() : model.getNightBg(),
                model.isDayMode());
    }

    /** Gọi bởi InputHandler khi người dùng bấm Replay. */
    private void handleReplay() {
        model.resetGame();
        deathAnim.reset();
    }

    /** Gọi bởi Observer khi GameEvent.QUIT_REQUESTED. */
    private void handleQuit() {
        loop.stop();
        audio.dispose();
        System.exit(0);
    }
}