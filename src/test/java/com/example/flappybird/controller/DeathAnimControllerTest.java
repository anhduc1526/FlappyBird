package com.example.flappybird.controller;

import com.example.flappybird.model.BirdModel;
import com.example.flappybird.util.GameConstants;
import com.example.flappybird.view.GameRenderer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests cho DeathAnimController.
 *
 * Dùng Mockito để mock GameRenderer — tránh khởi tạo JavaFX Canvas.
 *
 * Phạm vi kiểm thử:
 *  - Trạng thái ban đầu: IDLE, isActive=false, isInputReady=false
 *  - begin(): chuyển sang FALLING, snap angle=90, reset deathDy
 *  - advance() khi FALLING: chim rơi xuống
 *  - advance() chuyển SLIDING khi chim chạm đất
 *  - advance() chuyển WAITING sau khi panel slide xong
 *  - isInputReady() = true chỉ khi WAITING
 *  - reset() về IDLE
 *  - Renderer được gọi đúng method / argument
 */
@DisplayName("DeathAnimController — State Machine Tests")
@ExtendWith(MockitoExtension.class)
class DeathAnimControllerTest {

    @Mock
    private GameRenderer mockRenderer;

    private DeathAnimController controller;
    private BirdModel           bird;

    @BeforeEach
    void setUp() {
        controller = new DeathAnimController();
        bird       = new BirdModel();
    }

    // ── Trạng thái ban đầu ────────────────────────────────────────────────────

    @Test
    @DisplayName("Ban đầu không active (IDLE)")
    void initiallyNotActive() {
        assertFalse(controller.isActive(), "Phải ở IDLE trước begin()");
    }

    @Test
    @DisplayName("Ban đầu chưa sẵn sàng nhận input")
    void initiallyInputNotReady() {
        assertFalse(controller.isInputReady(), "Chưa WAITING → không nhận input");
    }

    @Test
    @DisplayName("advance() khi IDLE không gọi renderer")
    void advanceInIdleDoesNotCallRenderer() {
        controller.advance(mockRenderer, bird, 0, 0, -1);
        verifyNoInteractions(mockRenderer);
    }

    // ── begin() ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("begin() kích hoạt controller (isActive = true)")
    void beginActivatesController() {
        controller.begin(bird);
        assertTrue(controller.isActive());
    }

    @Test
    @DisplayName("begin() snap angle của bird về 90°")
    void beginSnapsAngleTo90() {
        bird.setAngle(15.0);
        controller.begin(bird);
        assertEquals(90.0, bird.getAngle(), 0.01, "Angle phải snap về 90° khi chết");
    }

    @Test
    @DisplayName("begin() sau đó isInputReady() vẫn false")
    void beginInputNotReadyYet() {
        controller.begin(bird);
        assertFalse(controller.isInputReady(), "Chưa đến WAITING → không nhận input");
    }

    @Test
    @DisplayName("begin() liên tiếp 2 lần vẫn hoạt động đúng (idempotent về angle)")
    void beginCalledTwiceIsIdempotent() {
        controller.begin(bird);
        bird.setAngle(45.0); // giả sử ai đó thay đổi angle
        controller.begin(bird);
        assertEquals(90.0, bird.getAngle(), 0.01, "begin() lần 2 phải snap lại về 90°");
    }

    // ── advance() FALLING ─────────────────────────────────────────────────────

    @Test
    @DisplayName("advance() khi FALLING làm chim rơi xuống (y tăng)")
    void fallingPhaseIncreasesY() {
        controller.begin(bird);
        double yBefore = bird.getY();
        controller.advance(mockRenderer, bird, 0, 0, -1);
        assertTrue(bird.getY() > yBefore, "Chim phải rơi xuống khi FALLING");
    }

    @Test
    @DisplayName("advance() khi FALLING không gọi drawGameOverPanel ngay lập tức")
    void fallingPhaseDoesNotShowPanel() {
        controller.begin(bird);
        // Bird ở giữa màn hình → chưa chạm đất → vẫn FALLING
        bird.setY(100); // xa đất
        controller.advance(mockRenderer, bird, 0, 0, -1);
        verify(mockRenderer, never()).drawGameOverPanel(anyInt(), anyInt(), anyInt(), anyDouble());
    }

    // ── FALLING → SLIDING ────────────────────────────────────────────────────

    @Test
    @DisplayName("Chuyển sang SLIDING (drawGameOverPanel được gọi) sau khi chạm đất")
    void transitionToSlidingAfterHittingGround() {
        controller.begin(bird);
        // Đặt bird ngay sát đất → FALLING ngay lập tức chuyển SLIDING
        bird.setY(GameConstants.LAND_Y - GameConstants.BIRD_HEIGHT - 1);
        controller.advance(mockRenderer, bird, 0, 0, -1);

        // Trong SLIDING: drawGameOverPanel được gọi với slideOffset > 0
        verify(mockRenderer, atLeastOnce())
                .drawGameOverPanel(anyInt(), anyInt(), anyInt(), anyDouble());
    }

    // ── SLIDING → WAITING ─────────────────────────────────────────────────────

    @Test
    @DisplayName("isInputReady() = true sau khi panel đã slide xong")
    void inputReadyAfterSlideComplete() {
        controller.begin(bird);
        bird.setY(GameConstants.LAND_Y - GameConstants.BIRD_HEIGHT - 1);

        // Chạy đủ tick để panelSlide: 220 → 0 (cần ~50-80 tick)
        for (int i = 0; i < 120; i++) {
            controller.advance(mockRenderer, bird, 5, 10, 1);
        }

        assertTrue(controller.isInputReady(), "Phải sẵn sàng nhận input sau slide");
    }

    @Test
    @DisplayName("advance() khi WAITING gọi drawGameOverPanel với slideOffset = 0")
    void waitingCallsRendererWithZeroOffset() {
        controller.begin(bird);
        bird.setY(GameConstants.LAND_Y - GameConstants.BIRD_HEIGHT - 1);

        for (int i = 0; i < 120; i++) {
            controller.advance(mockRenderer, bird, 3, 7, 0);
        }

        // Xóa lịch sử mock, kiểm tra tick WAITING tiếp theo
        clearInvocations(mockRenderer);
        controller.advance(mockRenderer, bird, 3, 7, 0);
        verify(mockRenderer).drawGameOverPanel(3, 7, 0, 0.0);
    }

    // ── reset() ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("reset() về IDLE: isActive = false")
    void resetDeactivatesController() {
        controller.begin(bird);
        controller.reset();
        assertFalse(controller.isActive());
    }

    @Test
    @DisplayName("reset() về IDLE: isInputReady = false")
    void resetClearsInputReady() {
        controller.begin(bird);
        bird.setY(GameConstants.LAND_Y - GameConstants.BIRD_HEIGHT - 1);
        for (int i = 0; i < 120; i++) {
            controller.advance(mockRenderer, bird, 0, 0, -1);
        }
        assertTrue(controller.isInputReady(), "Tiền điều kiện: phải sẵn sàng trước reset");

        controller.reset();
        assertFalse(controller.isInputReady(), "Sau reset(), phải không sẵn sàng");
    }

    @Test
    @DisplayName("Sau reset(), advance() không gọi renderer (trở về IDLE)")
    void afterResetAdvanceDoesNothing() {
        controller.begin(bird);
        controller.reset();
        clearInvocations(mockRenderer);

        controller.advance(mockRenderer, bird, 0, 0, -1);
        verifyNoInteractions(mockRenderer);
    }
}
