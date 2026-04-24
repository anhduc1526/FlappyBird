package com.example.flappybird.controller;

import com.example.flappybird.FlappyBirdApp;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TestFX Integration Tests cho GameController + InputHandler.
 *
 * Khởi động ứng dụng thực sự ở chế độ headless và giả lập input người dùng.
 *
 * Yêu cầu JVM flags (xem pom.xml / README):
 *   -Dtestfx.robot=glass
 *   -Dtestfx.headless=true
 *   -Dprism.order=sw
 *   --add-opens javafx.graphics/com.sun.glass.ui=ALL-UNNAMED
 *
 * Phạm vi kiểm thử:
 *  - Cửa sổ khởi động đúng tiêu đề, kích thước, không resize
 *  - Click chuột / phím SPACE / UP bắt đầu game
 *  - Phím P: pause / resume
 *  - Phím M: toggle âm thanh
 *  - Game loop chạy ổn định ít nhất 1 giây
 *  - Stress test: flap liên tục không crash
 */
@DisplayName("GameController — TestFX Integration Tests")
@ExtendWith(ApplicationExtension.class)
class GameControllerFXTest {

    private Stage primaryStage;

    /**
     * @Start được TestFX gọi trên JavaFX Application Thread.
     * Khởi tạo toàn bộ ứng dụng như khi người dùng chạy thực.
     */
    @Start
    void start(Stage stage) throws Exception {
        primaryStage = stage;
        new FlappyBirdApp().start(stage);
    }

    // ── Cửa sổ ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Cửa sổ hiển thị đúng tiêu đề 'Flappy Bird'")
    void windowTitleIsCorrect() {
        assertEquals("Flappy Bird", primaryStage.getTitle());
    }

    @Test
    @DisplayName("Cửa sổ có chiều rộng 350px")
    void windowWidthIsCorrect() {
        assertEquals(350, primaryStage.getWidth(), 5.0);
    }

    @Test
    @DisplayName("Cửa sổ có chiều cao 625px")
    void windowHeightIsCorrect() {
        assertEquals(625, primaryStage.getHeight(), 5.0);
    }

    @Test
    @DisplayName("Cửa sổ không cho phép thay đổi kích thước")
    void windowIsNotResizable() {
        assertFalse(primaryStage.isResizable());
    }

    @Test
    @DisplayName("Cửa sổ đang hiển thị sau khi khởi động")
    void windowIsShowing() {
        assertTrue(primaryStage.isShowing());
    }

    // ── Input: bắt đầu game ───────────────────────────────────────────────────

    @Test
    @DisplayName("Click chuột vào màn hình → không crash, cửa sổ vẫn hiển thị")
    void mouseClickDoesNotCrash(FxRobot robot) throws InterruptedException {
        robot.clickOn(primaryStage.getScene(), MouseButton.PRIMARY);
        Thread.sleep(100);
        assertTrue(primaryStage.isShowing());
    }

    @Test
    @DisplayName("Phím SPACE ở MENU → bắt đầu game, không crash")
    void spaceKeyStartsGame(FxRobot robot) throws InterruptedException {
        robot.press(KeyCode.SPACE).release(KeyCode.SPACE);
        Thread.sleep(100);
        assertTrue(primaryStage.isShowing());
    }

    @Test
    @DisplayName("Phím UP ở MENU → bắt đầu game, không crash")
    void upKeyStartsGame(FxRobot robot) throws InterruptedException {
        robot.press(KeyCode.UP).release(KeyCode.UP);
        Thread.sleep(100);
        assertTrue(primaryStage.isShowing());
    }

    // ── Input: Pause ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Phím P khi đang chơi → pause rồi resume, không crash")
    void pauseAndResumeWithKey(FxRobot robot) throws InterruptedException {
        // Bắt đầu game
        robot.press(KeyCode.SPACE).release(KeyCode.SPACE);
        Thread.sleep(200);

        // Pause
        robot.press(KeyCode.P).release(KeyCode.P);
        Thread.sleep(100);
        assertTrue(primaryStage.isShowing(), "Cửa sổ phải hiển thị sau P (pause)");

        // Resume
        robot.press(KeyCode.P).release(KeyCode.P);
        Thread.sleep(100);
        assertTrue(primaryStage.isShowing(), "Cửa sổ phải hiển thị sau P (resume)");
    }

    // ── Input: Sound Toggle ───────────────────────────────────────────────────

    @Test
    @DisplayName("Phím M tắt/bật âm thanh, không crash")
    void soundToggleWithKey(FxRobot robot) throws InterruptedException {
        robot.press(KeyCode.M).release(KeyCode.M);
        Thread.sleep(100);
        assertTrue(primaryStage.isShowing(), "Cửa sổ vẫn chạy sau khi tắt âm thanh");

        robot.press(KeyCode.M).release(KeyCode.M);
        Thread.sleep(100);
        assertTrue(primaryStage.isShowing(), "Cửa sổ vẫn chạy sau khi bật lại âm thanh");
    }

    // ── Game loop ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Game loop chạy ổn định 1 giây (~60 tick) không crash")
    void gameLoopRunsFor1Second(FxRobot robot) throws InterruptedException {
        robot.press(KeyCode.SPACE).release(KeyCode.SPACE);
        Thread.sleep(1000);
        assertTrue(primaryStage.isShowing(), "Game vẫn chạy sau 1 giây");
    }

    @Test
    @DisplayName("Flap liên tục 20 lần (stress test) không crash")
    void rapidFlapInputStressTest(FxRobot robot) throws InterruptedException {
        robot.press(KeyCode.SPACE).release(KeyCode.SPACE);
        Thread.sleep(50);

        for (int i = 0; i < 20; i++) {
            robot.press(KeyCode.SPACE).release(KeyCode.SPACE);
            Thread.sleep(16); // ~1 frame ở 60fps
        }

        assertTrue(primaryStage.isShowing(), "Game không crash khi flap liên tục");
    }

    @Test
    @DisplayName("Pause rồi flap rồi resume không bị treo")
    void pauseFlapResumeDoesNotFreeze(FxRobot robot) throws InterruptedException {
        robot.press(KeyCode.SPACE).release(KeyCode.SPACE);
        Thread.sleep(100);

        robot.press(KeyCode.P).release(KeyCode.P);
        Thread.sleep(50);

        // Flap khi đang pause — không làm gì (không crash)
        robot.press(KeyCode.SPACE).release(KeyCode.SPACE);
        Thread.sleep(50);

        robot.press(KeyCode.P).release(KeyCode.P);
        Thread.sleep(200);

        assertTrue(primaryStage.isShowing());
    }
}
