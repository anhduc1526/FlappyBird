package com.example.flappybird.model;

import com.example.flappybird.util.GameConstants;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests cho BirdModel.
 *
 * Phạm vi kiểm thử:
 *  - Trạng thái khởi tạo (vị trí, góc, frame animation)
 *  - Vật lý: trọng lực, giới hạn vận tốc rơi, va chạm đất/trần
 *  - Flap: đặt lại vận tốc & góc
 *  - Animation: chu kỳ frame
 *  - Death slide: slideDown() & resetDeathDy()
 *  - Thay đổi loại chim
 *  - reset() phục hồi toàn bộ trạng thái
 */
@DisplayName("BirdModel — Physics & State Tests")
class BirdModelTest {

    private BirdModel bird;

    @BeforeEach
    void setUp() {
        bird = new BirdModel();
    }

    // ── Khởi tạo ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Vị trí ban đầu khớp với GameConstants")
    void initialPosition() {
        assertEquals(GameConstants.BIRD_INITIAL_X, bird.getX(), 0.01,
                "X ban đầu phải bằng BIRD_INITIAL_X");
        assertEquals(GameConstants.BIRD_INITIAL_Y, bird.getY(), 0.01,
                "Y ban đầu phải bằng BIRD_INITIAL_Y");
    }

    @Test
    @DisplayName("Góc và kiểu chim ban đầu là 0")
    void initialAngleAndType() {
        assertEquals(0.0, bird.getAngle(), 0.01, "Góc ban đầu phải là 0");
        assertEquals(0, bird.getTypeBird(), "Kiểu chim ban đầu phải là 0 (yellow)");
    }

    @Test
    @DisplayName("Boundary helper: getRight() và getBottom() đúng kích thước")
    void boundaryHelpers() {
        assertEquals(bird.getX() + GameConstants.BIRD_WIDTH,  bird.getRight(),  0.01);
        assertEquals(bird.getY() + GameConstants.BIRD_HEIGHT, bird.getBottom(), 0.01);
    }

    // ── Vật lý: trọng lực ─────────────────────────────────────────────────────

    @Test
    @DisplayName("move() khiến chim rơi xuống theo trọng lực")
    void moveAppliesGravity() {
        double yBefore = bird.getY();
        bird.move();
        assertTrue(bird.getY() > yBefore, "Chim phải rơi xuống sau 1 tick");
    }

    @Test
    @DisplayName("Sau nhiều tick, chim không rơi qua đất (LAND_Y)")
    void moveDoesNotExceedLandY() {
        bird.setY(GameConstants.LAND_Y - GameConstants.BIRD_HEIGHT - 1);
        for (int i = 0; i < 30; i++) bird.move();
        assertTrue(bird.getY() <= GameConstants.LAND_Y - GameConstants.BIRD_HEIGHT,
                "Chim không được xuyên qua đất");
    }

    @Test
    @DisplayName("Chim không rơi lên trên trần (y < 0)")
    void moveDoesNotExceedCeiling() {
        for (int i = 0; i < 20; i++) bird.flapUp();
        for (int i = 0; i < 5;  i++) bird.move();
        assertTrue(bird.getY() >= 0, "Chim không được lên trên trần màn hình");
    }

    // ── Flap ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("flapUp() đặt góc về -30 độ")
    void flapSetsAngle() {
        bird.flapUp();
        assertEquals(-30.0, bird.getAngle(), 0.01, "Góc sau khi flap phải là -30°");
    }

    @Test
    @DisplayName("flapUp() làm chim bay lên sau 1 tick")
    void flapCausesUpwardMovement() {
        double yBefore = bird.getY();
        bird.flapUp();
        bird.move();
        assertTrue(bird.getY() < yBefore, "Chim phải bay lên sau khi flap");
    }

    // ── Animation ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAnimationFrame() luôn nằm trong [0, ANIM_FRAME_COUNT)")
    void animationFrameInRange() {
        for (int i = 0; i < GameConstants.ANIM_FRAME_COUNT * 3; i++) {
            bird.updateAnimation();
            int frame = bird.getAnimationFrame();
            assertTrue(frame >= 0 && frame < GameConstants.ANIM_FRAME_COUNT,
                    "Frame animation phải nằm trong chu kỳ hợp lệ");
        }
    }

    @Test
    @DisplayName("Animation frame về 0 sau đúng 1 chu kỳ ANIM_FRAME_COUNT")
    void animationCycles() {
        for (int i = 0; i < GameConstants.ANIM_FRAME_COUNT; i++) bird.updateAnimation();
        assertEquals(0, bird.getAnimationFrame(), "Frame phải quay về 0 sau 1 chu kỳ");
    }

    // ── Death slide ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("slideDown() tăng dần y mỗi lần gọi")
    void slideDownIncreasesY() {
        double y0 = bird.getY();
        bird.slideDown();
        double y1 = bird.getY();
        bird.slideDown();
        double y2 = bird.getY();
        assertTrue(y1 > y0, "Y phải tăng sau lần slideDown() đầu tiên");
        assertTrue(y2 > y1, "Y phải tiếp tục tăng sau lần slideDown() thứ hai");
    }

    @Test
    @DisplayName("resetDeathDy() đặt lại tốc độ rơi về giá trị ban đầu")
    void resetDeathDyRestoresInitialSpeed() {
        for (int i = 0; i < 20; i++) bird.slideDown();
        bird.resetDeathDy();
        double y0 = bird.getY();
        bird.slideDown();
        assertTrue(bird.getY() - y0 < 10.0, "Sau resetDeathDy(), tốc độ rơi phải nhỏ");
    }

    // ── Thay đổi kiểu chim ────────────────────────────────────────────────────

    @ParameterizedTest(name = "Thay đổi +{0} lần")
    @ValueSource(ints = {1, 2, 3, 100})
    @DisplayName("changeTypeBird() luôn ở phạm vi hợp lệ")
    void changeTypeBirdWraps(int times) {
        for (int i = 0; i < times; i++) bird.changeTypeBird(1);
        int type = bird.getTypeBird();
        assertTrue(type >= 0 && type < GameConstants.BIRD_TYPE_COUNT,
                "Kiểu chim phải nằm trong [0, BIRD_TYPE_COUNT)");
    }

    @Test
    @DisplayName("changeTypeBird(-1) hoạt động đúng chiều ngược lại")
    void changeTypeBirdBackward() {
        bird.changeTypeBird(-1);
        assertEquals(GameConstants.BIRD_TYPE_COUNT - 1, bird.getTypeBird());
        bird.changeTypeBird(-1);
        assertEquals(GameConstants.BIRD_TYPE_COUNT - 2, bird.getTypeBird());
    }

    // ── reset() ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("reset() phục hồi vị trí và góc về ban đầu")
    void resetRestoresPositionAndAngle() {
        bird.flapUp();
        for (int i = 0; i < 10; i++) { bird.move(); bird.updateAnimation(); }
        bird.setAngle(75);
        bird.setY(300);
        bird.reset();

        assertAll("Trạng thái sau reset()",
                () -> assertEquals(GameConstants.BIRD_INITIAL_X, bird.getX(),     0.01),
                () -> assertEquals(GameConstants.BIRD_INITIAL_Y, bird.getY(),     0.01),
                () -> assertEquals(0.0,                           bird.getAngle(), 0.01),
                () -> assertEquals(0,                             bird.getAnimationFrame())
        );
    }

    @Test
    @DisplayName("reset() không đặt lại typeBird — người chơi giữ skin đã chọn")
    void resetKeepsBirdType() {
        bird.changeTypeBird(1);
        bird.reset();
        assertEquals(1, bird.getTypeBird(),
                "typeBird phải được giữ nguyên sau reset() để người chơi không mất skin");
    }
}
