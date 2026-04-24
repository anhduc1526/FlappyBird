package com.example.flappybird.model;

import com.example.flappybird.util.GameConstants;
import com.example.flappybird.util.ScoreManager;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

/**
 * MODEL — Central game state.
 *
 * Sở hữu tất cả sub-models: bird, pipes, backgrounds, score.
 * Chứa toàn bộ game-logic (collision, scoring, pipe recycling).
 * Không phụ thuộc JavaFX / rendering.
 *
 * Thay đổi so với phiên bản cũ:
 *  - Thêm Observer pattern: fire(GameEvent) thay vì Controller polling getPhase()
 *  - togglePause() gộp pauseGame() + resumeGame()
 *  - Xoá trường quit (thay bằng GameEvent.QUIT_REQUESTED)
 *
 * Design Pattern: Facade + Observer.
 */
public class GameModel {

    // ── Sub-models ────────────────────────────────────────────────────────────
    private final BirdModel            bird  = new BirdModel();
    private final List<PipeModel>      pipes = new ArrayList<>();
    private final ScrollingBackground  dayBg   = new ScrollingBackground(288);
    private final ScrollingBackground  nightBg = new ScrollingBackground(288);
    private final ScrollingBackground  land    = new ScrollingBackground(336);

    // ── Game state ────────────────────────────────────────────────────────────
    public enum Phase { MENU, PLAYING, DEAD, PAUSED }

    private Phase   phase     = Phase.MENU;
    private int     score     = 0;
    private int     bestScore = 0;
    private boolean dayMode   = true;
    private boolean soundOn   = true;

    private final Random rng = new Random();

    // ── Observer registry ─────────────────────────────────────────────────────
    // EnumMap giữ list listener riêng cho từng event → dispatch O(listeners) không duyệt toàn bộ enum
    private final Map<GameEvent, List<Consumer<GameEvent>>> listeners =
            new EnumMap<>(GameEvent.class);

    // ── Constructor ───────────────────────────────────────────────────────────

    public GameModel() {
        bestScore = ScoreManager.loadBestScore();
        initPipes(GameConstants.PIPE_START_X);
    }

    // ── Observer API ──────────────────────────────────────────────────────────

    /**
     * Đăng ký lắng nghe một sự kiện cụ thể.
     * <pre>
     *   model.on(GameEvent.BIRD_DIED,  e -> beginDeathAnimation());
     *   model.on(GameEvent.SCORE_UPDATED, e -> audio.playPoint());
     * </pre>
     */
    public void on(GameEvent event, Consumer<GameEvent> listener) {
        listeners.computeIfAbsent(event, k -> new ArrayList<>()).add(listener);
    }

    /** Gỡ toàn bộ listener (dùng khi reset hoặc test). */
    public void clearListeners() {
        listeners.clear();
    }

    private void fire(GameEvent event) {
        List<Consumer<GameEvent>> list = listeners.get(event);
        if (list != null) list.forEach(l -> l.accept(event));
    }

    // ── Pipe helpers ──────────────────────────────────────────────────────────

    private void initPipes(int startX) {
        pipes.clear();
        for (int i = 0; i < GameConstants.PIPE_COUNT; i++) {
            pipes.add(new PipeModel(
                    startX + (long) GameConstants.PIPE_DISTANCE * i,
                    rng.nextInt(GameConstants.PIPE_Y_RANGE)));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Game-tick update — gọi bởi controller ở 60 fps
    // ─────────────────────────────────────────────────────────────────────────

    /** Advance một tick khi phase == PLAYING. */
    public void tick() {
        bird.move();

        dayBg.scroll(GameConstants.SCROLL_SPEED);
        nightBg.scroll(GameConstants.SCROLL_SPEED);
        land.scroll(GameConstants.SCROLL_SPEED);

        for (PipeModel p : pipes) p.scroll(GameConstants.SCROLL_SPEED);

        // Recycle pipe ra khỏi màn hình
        if (pipes.get(0).isOffScreen()) {
            pipes.remove(0);
            double newX = pipes.get(pipes.size() - 1).getX() + GameConstants.PIPE_DISTANCE;
            pipes.add(new PipeModel(newX, rng.nextInt(GameConstants.PIPE_Y_RANGE)));
        }

        // Tính điểm
        if (pipes.get(0).birdJustPassed(bird)) {
            score++;
            fire(GameEvent.SCORE_UPDATED);
        }

        // Kiểm tra va chạm
        boolean pipeHit  = pipes.get(0).collidesWith(bird);
        boolean floorHit = bird.getY() >= GameConstants.LAND_Y - GameConstants.BIRD_HEIGHT;
        if (pipeHit || floorHit) {
            if (score > bestScore) {
                bestScore = score;
                ScoreManager.saveBestScore(bestScore);
            }
            phase = Phase.DEAD;
            fire(GameEvent.BIRD_DIED);
        }
    }

    /** Tick dùng khi ở menu (cuộn nền, không có physics). */
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

    /** Gộp pauseGame + resumeGame vào một toggle cho gọn. */
    public void togglePause() {
        if (phase == Phase.PLAYING) {
            phase = Phase.PAUSED;
            fire(GameEvent.GAME_PAUSED);
        } else if (phase == Phase.PAUSED) {
            phase = Phase.PLAYING;
            fire(GameEvent.GAME_RESUMED);
        }
    }

    /** Giữ lại để tương thích nếu cần gọi riêng lẻ. */
    public void pauseGame()  { if (phase == Phase.PLAYING) { phase = Phase.PAUSED;  fire(GameEvent.GAME_PAUSED);  } }
    public void resumeGame() { if (phase == Phase.PAUSED)  { phase = Phase.PLAYING; fire(GameEvent.GAME_RESUMED); } }

    public void resetGame() {
        bird.reset();
        score = 0;
        initPipes(GameConstants.PIPE_RESET_X);
        phase = Phase.MENU;
        fire(GameEvent.GAME_RESET);
    }

    public void requestQuit() {
        fire(GameEvent.QUIT_REQUESTED);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Input delegation
    // ─────────────────────────────────────────────────────────────────────────

    public void birdFlap() {
        if (phase == Phase.PLAYING) bird.flapUp();
    }

    public void birdTurnDown() {
        if (phase == Phase.PLAYING) bird.turnDown();
    }

    public void changeBirdType(int delta) { bird.changeTypeBird(delta); }

    public void toggleDay()   { dayMode  = !dayMode; }

    public void toggleSound() {
        soundOn = !soundOn;
        fire(GameEvent.SOUND_TOGGLED);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Getters
    // ─────────────────────────────────────────────────────────────────────────

    public BirdModel           getBird()      { return bird;      }
    public List<PipeModel>     getPipes()     { return pipes;     }
    public ScrollingBackground getDayBg()     { return dayBg;     }
    public ScrollingBackground getNightBg()   { return nightBg;   }
    public ScrollingBackground getLand()      { return land;      }
    public Phase               getPhase()     { return phase;     }
    public int                 getScore()     { return score;     }
    public int                 getBestScore() { return bestScore; }
    public boolean             isDayMode()    { return dayMode;   }
    public boolean             isSoundOn()    { return soundOn;   }

    /** Medal index: 0=bronze, 1=silver, 2=gold; -1=none. */
    public int getMedalIndex() {
        if (score >= GameConstants.MEDAL_GOLD_MIN)   return 2;
        if (score >= GameConstants.MEDAL_SILVER_MIN) return 1;
        if (score >= GameConstants.MEDAL_BRONZE_MIN) return 0;
        return -1;
    }
}