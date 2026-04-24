package com.example.flappybird.model;

import com.example.flappybird.util.GameConstants;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests cho PipeModel.
 *
 * Phạm vi kiểm thử:
 *  - Vị trí khởi tạo (upperY, lowerY theo posY)
 *  - scroll() dịch chuyển x sang trái
 *  - isOffScreen() khi pipe ra ngoài rìa trái
 *  - collidesWith() — hit-test đầy đủ (upper, lower, bay qua an toàn)
 *  - birdJustPassed() — tính điểm đúng thời điểm
 */
@DisplayName("PipeModel — Scroll, Collision & Scoring Tests")
class PipeModelTest {

    private BirdModel bird;
    private PipeModel pipe;

    @BeforeEach
    void setUp() {
        pipe = new PipeModel(200, 0);
        bird = new BirdModel();
    }

    // ── Khởi tạo ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("upperY = PIPE_UPPER_BASE + posY")
    void upperYCalculation() {
        PipeModel p = new PipeModel(0, 50);
        assertEquals(GameConstants.PIPE_UPPER_BASE + 50, p.getUpperY(), 0.01);
    }

    @Test
    @DisplayName("lowerY = PIPE_LOWER_BASE + posY")
    void lowerYCalculation() {
        PipeModel p = new PipeModel(0, 50);
        assertEquals(GameConstants.PIPE_LOWER_BASE + 50, p.getLowerY(), 0.01);
    }

    @Test
    @DisplayName("getX() trả về đúng x truyền vào constructor")
    void getXReturnsInitialX() {
        assertEquals(200.0, pipe.getX(), 0.01);
    }

    // ── scroll() ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("scroll(2) giảm x đi 2 mỗi tick")
    void scrollMovesLeft() {
        double xBefore = pipe.getX();
        pipe.scroll(2);
        assertEquals(xBefore - 2, pipe.getX(), 0.01);
    }

    @Test
    @DisplayName("scroll() liên tiếp tích lũy đúng tổng khoảng cách")
    void scrollAccumulates() {
        double xBefore = pipe.getX();
        int ticks = 100;
        for (int i = 0; i < ticks; i++) pipe.scroll(GameConstants.SCROLL_SPEED);
        assertEquals(xBefore - (long) ticks * GameConstants.SCROLL_SPEED,
                pipe.getX(), 0.01);
    }

    // ── isOffScreen() ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("isOffScreen() false khi pipe chưa ra khỏi màn hình")
    void notOffScreenInitially() {
        assertFalse(pipe.isOffScreen());
    }

    @Test
    @DisplayName("isOffScreen() true khi x + PIPE_WIDTH <= 0")
    void offScreenWhenScrolledPast() {
        pipe.setPositionX(-(GameConstants.PIPE_WIDTH + 1));
        assertTrue(pipe.isOffScreen());
    }

    @Test
    @DisplayName("isOffScreen() false khi còn 1px trên màn hình")
    void notOffScreenWhenPartiallyVisible() {
        pipe.setPositionX(-GameConstants.PIPE_WIDTH + 1);
        assertFalse(pipe.isOffScreen());
    }

    @Test
    @DisplayName("isOffScreen() false khi x = 0 (cạnh trái màn hình)")
    void notOffScreenAtLeftEdge() {
        pipe.setPositionX(0);
        assertFalse(pipe.isOffScreen());
    }

    // ── collidesWith() ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Không va chạm khi bird ở xa bên phải pipe")
    void noCollisionWhenBirdFarRight() {
        bird.setY(250);
        PipeModel farPipe = new PipeModel(500, 0);
        assertFalse(farPipe.collidesWith(bird));
    }

    @Test
    @DisplayName("Không va chạm khi bird bay qua gap an toàn")
    void noCollisionThroughGap() {
        // Gap an toàn: bên dưới upper pipe bottom và bên trên lower pipe top
        // upperBottom = PIPE_UPPER_BASE + PIPE_HEIGHT = -220 + 320 = 100
        // lowerTop    = PIPE_LOWER_BASE               = 270
        // Đặt bird y = 150 → hoàn toàn trong gap
        BirdModel gapBird = new BirdModel();
        gapBird.setY(150);
        PipeModel alignedPipe = new PipeModel((int) gapBird.getX(), 0);
        assertFalse(alignedPipe.collidesWith(gapBird),
                "Bird trong gap không được va chạm với pipe");
    }

    @Test
    @DisplayName("Va chạm với upper pipe")
    void collisionWithUpperPipe() {
        BirdModel hitBird = new BirdModel();
        // upper pipe: top = -220, bottom = -220 + 320 = 100
        // Đặt bird y = 80 → bird.bottom = 80 + 24 = 104 > 100 → chạm
        hitBird.setY(80);
        PipeModel alignedPipe = new PipeModel((int) hitBird.getX(), 0);
        assertTrue(alignedPipe.collidesWith(hitBird),
                "Phải phát hiện va chạm với upper pipe");
    }

    @Test
    @DisplayName("Va chạm với lower pipe")
    void collisionWithLowerPipe() {
        BirdModel hitBird = new BirdModel();
        // lower pipe top = 270 → đặt bird y = 270 → trong lower pipe
        hitBird.setY(270);
        PipeModel alignedPipe = new PipeModel((int) hitBird.getX(), 0);
        assertTrue(alignedPipe.collidesWith(hitBird),
                "Phải phát hiện va chạm với lower pipe");
    }

    // ── birdJustPassed() ──────────────────────────────────────────────────────

    @Test
    @DisplayName("birdJustPassed() true khi birdX trùng pipeX (tính điểm)")
    void birdJustPassedWhenAligned() {
        PipeModel scorePipe = new PipeModel(GameConstants.BIRD_INITIAL_X, 0);
        BirdModel scoreBird = new BirdModel(); // x = BIRD_INITIAL_X
        assertTrue(scorePipe.birdJustPassed(scoreBird));
    }

    @Test
    @DisplayName("birdJustPassed() false khi bird chưa đến vị trí pipe")
    void birdNotYetPassed() {
        PipeModel farPipe = new PipeModel(GameConstants.BIRD_INITIAL_X + 100, 0);
        assertFalse(farPipe.birdJustPassed(new BirdModel()));
    }

    @Test
    @DisplayName("birdJustPassed() false khi bird đã qua pipe")
    void birdAlreadyPassed() {
        PipeModel behindPipe = new PipeModel(GameConstants.BIRD_INITIAL_X - 50, 0);
        assertFalse(behindPipe.birdJustPassed(new BirdModel()));
    }
}
