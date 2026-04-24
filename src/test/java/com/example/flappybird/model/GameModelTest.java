package com.example.flappybird.model;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests cho GameModel — State Machine + Observer Pattern.
 *
 * Phạm vi kiểm thử:
 *  - Phase transitions: MENU → PLAYING → PAUSED → PLAYING → DEAD → MENU
 *  - togglePause() / pauseGame() / resumeGame()
 *  - Observer: fire đúng event khi đúng transition
 *  - birdFlap() chỉ hoạt động khi PLAYING
 *  - toggleDay() / toggleSound()
 *  - getMedalIndex() theo ngưỡng điểm
 *  - resetGame() reset toàn bộ state
 *  - Pipe list không rỗng
 */
@DisplayName("GameModel — State Machine & Observer Tests")
@ExtendWith(MockitoExtension.class)
class GameModelTest {

    private GameModel model;

    @BeforeEach
    void setUp() {
        model = new GameModel();
    }

    // ── Phase ban đầu ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Phase ban đầu là MENU")
    void initialPhaseIsMenu() {
        assertEquals(GameModel.Phase.MENU, model.getPhase());
    }

    @Test
    @DisplayName("Score ban đầu là 0")
    void initialScoreIsZero() {
        assertEquals(0, model.getScore());
    }

    @Test
    @DisplayName("dayMode ban đầu là true")
    void initialDayModeIsTrue() {
        assertTrue(model.isDayMode());
    }

    @Test
    @DisplayName("soundOn ban đầu là true")
    void initialSoundIsOn() {
        assertTrue(model.isSoundOn());
    }

    @Test
    @DisplayName("getPipes() không rỗng sau khi khởi tạo")
    void pipesInitiallyPresent() {
        assertFalse(model.getPipes().isEmpty(), "Phải có pipe ngay từ đầu");
    }

    @Test
    @DisplayName("getBird() không null")
    void birdNotNull() {
        assertNotNull(model.getBird());
    }

    // ── startGame() ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("startGame() chuyển MENU → PLAYING")
    void startGameChangesPhase() {
        model.startGame();
        assertEquals(GameModel.Phase.PLAYING, model.getPhase());
    }

    // ── togglePause() ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("togglePause() từ PLAYING → PAUSED")
    void togglePauseFromPlaying() {
        model.startGame();
        model.togglePause();
        assertEquals(GameModel.Phase.PAUSED, model.getPhase());
    }

    @Test
    @DisplayName("togglePause() từ PAUSED → PLAYING")
    void togglePauseFromPaused() {
        model.startGame();
        model.pauseGame();
        model.togglePause();
        assertEquals(GameModel.Phase.PLAYING, model.getPhase());
    }

    @Test
    @DisplayName("togglePause() không làm gì khi phase là MENU")
    void togglePauseInMenuDoesNothing() {
        model.togglePause();
        assertEquals(GameModel.Phase.MENU, model.getPhase());
    }

    @Test
    @DisplayName("pauseGame() không làm gì khi phase là MENU")
    void pauseGameInMenuDoesNothing() {
        model.pauseGame();
        assertEquals(GameModel.Phase.MENU, model.getPhase());
    }

    @Test
    @DisplayName("resumeGame() không làm gì khi phase là PLAYING")
    void resumeGameInPlayingDoesNothing() {
        model.startGame();
        model.resumeGame(); // đã PLAYING → không đổi
        assertEquals(GameModel.Phase.PLAYING, model.getPhase());
    }

    // ── Observer: GAME_PAUSED / GAME_RESUMED ──────────────────────────────────

    @Test
    @DisplayName("Observer nhận GAME_PAUSED khi pauseGame()")
    void observerFiresGamePaused() {
        List<GameEvent> received = new ArrayList<>();
        model.on(GameEvent.GAME_PAUSED, received::add);

        model.startGame();
        model.pauseGame();

        assertEquals(1, received.size());
        assertEquals(GameEvent.GAME_PAUSED, received.get(0));
    }

    @Test
    @DisplayName("Observer nhận GAME_RESUMED khi resumeGame()")
    void observerFiresGameResumed() {
        List<GameEvent> received = new ArrayList<>();
        model.on(GameEvent.GAME_RESUMED, received::add);

        model.startGame();
        model.pauseGame();
        model.resumeGame();

        assertEquals(1, received.size());
        assertEquals(GameEvent.GAME_RESUMED, received.get(0));
    }

    @Test
    @DisplayName("Observer nhận SOUND_TOGGLED hai lần khi toggleSound() x2")
    void observerFiresSoundToggled() {
        AtomicInteger count = new AtomicInteger(0);
        model.on(GameEvent.SOUND_TOGGLED, e -> count.incrementAndGet());

        model.toggleSound();
        model.toggleSound();

        assertEquals(2, count.get());
        assertTrue(model.isSoundOn(), "Sau 2 lần toggle, âm thanh phải trở về bật");
    }

    @Test
    @DisplayName("Observer nhận GAME_RESET sau resetGame()")
    void observerFiresGameReset() {
        List<GameEvent> received = new ArrayList<>();
        model.on(GameEvent.GAME_RESET, received::add);
        model.resetGame();
        assertFalse(received.isEmpty(), "GAME_RESET phải được fire");
    }

    @Test
    @DisplayName("Observer nhận QUIT_REQUESTED sau requestQuit()")
    void observerFiresQuitRequested() {
        List<GameEvent> received = new ArrayList<>();
        model.on(GameEvent.QUIT_REQUESTED, received::add);
        model.requestQuit();
        assertEquals(1, received.size());
        assertEquals(GameEvent.QUIT_REQUESTED, received.get(0));
    }

    @Test
    @DisplayName("Nhiều observer cùng 1 sự kiện đều được gọi")
    void multipleObserversAllFired() {
        AtomicInteger count = new AtomicInteger(0);
        model.on(GameEvent.SOUND_TOGGLED, e -> count.incrementAndGet());
        model.on(GameEvent.SOUND_TOGGLED, e -> count.incrementAndGet());
        model.on(GameEvent.SOUND_TOGGLED, e -> count.incrementAndGet());

        model.toggleSound();
        assertEquals(3, count.get(), "Cả 3 observer phải được gọi");
    }

    @Test
    @DisplayName("clearListeners() xóa toàn bộ observer")
    void clearListenersRemovesAll() {
        AtomicInteger count = new AtomicInteger(0);
        model.on(GameEvent.SOUND_TOGGLED, e -> count.incrementAndGet());
        model.clearListeners();
        model.toggleSound();
        assertEquals(0, count.get());
    }

    // ── birdFlap() ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("birdFlap() không làm gì khi MENU (angle không đổi)")
    void birdFlapInMenuDoesNothing() {
        model.birdFlap();
        assertEquals(0.0, model.getBird().getAngle(), 0.01,
                "Angle không đổi khi birdFlap() ở MENU");
    }

    @Test
    @DisplayName("birdFlap() hoạt động khi PLAYING — angle = -30°")
    void birdFlapInPlayingWorks() {
        model.startGame();
        model.birdFlap();
        assertEquals(-30.0, model.getBird().getAngle(), 0.01);
    }

    // ── toggleDay() ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("toggleDay() chuyển đổi qua lại giữa day và night")
    void toggleDaySwitchesMode() {
        assertTrue(model.isDayMode());
        model.toggleDay();
        assertFalse(model.isDayMode());
        model.toggleDay();
        assertTrue(model.isDayMode());
    }

    // ── getMedalIndex() ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getMedalIndex() = -1 khi score = 0 (không có huy chương)")
    void noMedalWhenScoreIsZero() {
        assertEquals(-1, model.getMedalIndex());
    }

    @Test
    @DisplayName("getMedalIndex() = 0 (bronze) khi score = MEDAL_BRONZE_MIN")
    void bronzeMedalAtThreshold() {
        assertEquals(0, new GameModelForTest(10).getMedalIndex());
    }

    @Test
    @DisplayName("getMedalIndex() = 1 (silver) khi score = MEDAL_SILVER_MIN")
    void silverMedalAtThreshold() {
        assertEquals(1, new GameModelForTest(20).getMedalIndex());
    }

    @Test
    @DisplayName("getMedalIndex() = 2 (gold) khi score = MEDAL_GOLD_MIN")
    void goldMedalAtThreshold() {
        assertEquals(2, new GameModelForTest(30).getMedalIndex());
    }

    @Test
    @DisplayName("getMedalIndex() = 2 (gold) khi score vượt MEDAL_GOLD_MIN")
    void goldMedalAboveThreshold() {
        assertEquals(2, new GameModelForTest(100).getMedalIndex());
    }

    // ── resetGame() ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("resetGame() đặt phase về MENU")
    void resetGameRestoresPhase() {
        model.startGame();
        model.resetGame();
        assertEquals(GameModel.Phase.MENU, model.getPhase());
    }

    @Test
    @DisplayName("resetGame() đặt score về 0")
    void resetGameRestoresScore() {
        model.startGame();
        model.resetGame();
        assertEquals(0, model.getScore());
    }

    @Test
    @DisplayName("resetGame() tạo lại danh sách pipe")
    void resetGameRecreatesPipes() {
        assertFalse(model.getPipes().isEmpty());
        model.resetGame();
        assertFalse(model.getPipes().isEmpty(), "Pipe phải được tạo lại sau reset");
    }

    // ── Helper: GameModel override getScore() để test getMedalIndex() ─────────

    /**
     * Subclass test-only: override getScore() để kiểm tra getMedalIndex()
     * mà không cần chạy tick thực sự.
     */
    static class GameModelForTest extends GameModel {
        private final int fixedScore;
        GameModelForTest(int score) { this.fixedScore = score; }
        @Override public int getScore() { return fixedScore; }
    }
}
