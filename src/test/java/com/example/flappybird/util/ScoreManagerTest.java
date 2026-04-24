package com.example.flappybird.util;

import org.junit.jupiter.api.*;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests cho ScoreManager — đọc/ghi file điểm cao.
 *
 * Phạm vi kiểm thử:
 *  - loadBestScore() = 0 khi chưa có file
 *  - loadBestScore() đọc đúng giá trị từ file hợp lệ
 *  - loadBestScore() chịu đựng file rỗng / dữ liệu không hợp lệ / whitespace
 *  - saveBestScore() ghi đúng giá trị ra file
 *  - saveBestScore() ghi đè điểm cũ
 *  - Round-trip: save rồi load cho cùng giá trị
 */
@DisplayName("ScoreManager — Persistence Tests")
class ScoreManagerTest {

    private Path scoreFile;

    @BeforeEach
    void setUp() throws Exception {
        scoreFile = Path.of(GameConstants.SCORE_FILE);
        Files.deleteIfExists(scoreFile); // đảm bảo môi trường sạch
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(scoreFile); // dọn dẹp sau mỗi test
    }

    // ── loadBestScore() ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Trả về 0 khi chưa có file điểm cao")
    void loadReturnsZeroWhenNoFile() {
        assertEquals(0, ScoreManager.loadBestScore());
    }

    @Test
    @DisplayName("Đọc đúng điểm từ file hợp lệ")
    void loadReadsValidScore() throws Exception {
        Files.writeString(scoreFile, "42");
        assertEquals(42, ScoreManager.loadBestScore());
    }

    @Test
    @DisplayName("Trả về 0 khi file rỗng")
    void loadReturnsZeroForEmptyFile() throws Exception {
        Files.writeString(scoreFile, "");
        assertEquals(0, ScoreManager.loadBestScore());
    }

    @Test
    @DisplayName("Trả về 0 khi file chứa dữ liệu không phải số")
    void loadReturnsZeroForInvalidData() throws Exception {
        Files.writeString(scoreFile, "không phải số");
        assertEquals(0, ScoreManager.loadBestScore());
    }

    @Test
    @DisplayName("Bỏ qua whitespace thừa trong file")
    void loadTrimsWhitespace() throws Exception {
        Files.writeString(scoreFile, "  99  \n");
        assertEquals(99, ScoreManager.loadBestScore());
    }

    @Test
    @DisplayName("Đọc đúng khi file chứa số 0")
    void loadReadsZeroScore() throws Exception {
        Files.writeString(scoreFile, "0");
        assertEquals(0, ScoreManager.loadBestScore());
    }

    @Test
    @DisplayName("Đọc đúng số điểm cao lớn (>= 100)")
    void loadReadsLargeScore() throws Exception {
        Files.writeString(scoreFile, "9999");
        assertEquals(9999, ScoreManager.loadBestScore());
    }

    // ── saveBestScore() ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Ghi điểm vào file và đọc lại đúng giá trị")
    void savePersistsScore() {
        ScoreManager.saveBestScore(77);
        assertEquals(77, ScoreManager.loadBestScore());
    }

    @Test
    @DisplayName("Ghi đúng khi score = 0")
    void saveZeroScore() {
        ScoreManager.saveBestScore(0);
        assertEquals(0, ScoreManager.loadBestScore());
    }

    @Test
    @DisplayName("Ghi đè điểm cũ")
    void saveOverwritesPreviousScore() {
        ScoreManager.saveBestScore(10);
        ScoreManager.saveBestScore(25);
        assertEquals(25, ScoreManager.loadBestScore());
    }

    @Test
    @DisplayName("File tồn tại sau khi saveBestScore()")
    void fileExistsAfterSave() {
        ScoreManager.saveBestScore(5);
        assertTrue(Files.exists(scoreFile), "File điểm cao phải được tạo ra");
    }

    // ── Round-trip ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("save → load là đúng với nhiều giá trị khác nhau")
    void saveLoadRoundTrip() {
        int[] scores = {0, 1, 9, 10, 19, 20, 29, 30, 50, 100};
        for (int expected : scores) {
            ScoreManager.saveBestScore(expected);
            assertEquals(expected, ScoreManager.loadBestScore(),
                    "Round-trip thất bại cho score = " + expected);
        }
    }

    @Test
    @DisplayName("save → load → save → load giữ giá trị cuối cùng")
    void doubleRoundTrip() {
        ScoreManager.saveBestScore(30);
        ScoreManager.loadBestScore(); // không làm thay đổi
        ScoreManager.saveBestScore(45);
        assertEquals(45, ScoreManager.loadBestScore());
    }
}
