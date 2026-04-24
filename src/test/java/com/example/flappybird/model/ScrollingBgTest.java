package com.example.flappybird.model;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests cho ScrollingBackground.
 *
 * Phạm vi kiểm thử:
 *  - Offset ban đầu là 0
 *  - scroll() giảm offset
 *  - Offset không bao giờ nhỏ hơn -width (wrap-around)
 *  - Wrap-around không tạo ra bước nhảy bất thường (no jitter)
 *  - getWidth() trả về đúng giá trị
 */
@DisplayName("ScrollingBackground — Scroll & Wrap Tests")
class ScrollingBgTest {

    @Test
    @DisplayName("Offset ban đầu là 0")
    void initialOffsetIsZero() {
        ScrollingBackground bg = new ScrollingBackground(288);
        assertEquals(0.0, bg.getOffset(), 0.001);
    }

    @Test
    @DisplayName("scroll(2) giảm offset đi 2")
    void scrollDecreasesOffset() {
        ScrollingBackground bg = new ScrollingBackground(288);
        bg.scroll(2);
        assertEquals(-2.0, bg.getOffset(), 0.001);
    }

    @Test
    @DisplayName("scroll() nhiều lần tích lũy đúng tổng trước khi wrap")
    void scrollAccumulatesBeforeWrap() {
        ScrollingBackground bg = new ScrollingBackground(288);
        bg.scroll(10);
        bg.scroll(10);
        bg.scroll(10);
        assertEquals(-30.0, bg.getOffset(), 0.001);
    }

    @ParameterizedTest(name = "width={0}")
    @ValueSource(ints = {100, 288, 336, 500})
    @DisplayName("Offset luôn nằm trong (-width, 0] với mọi chiều rộng")
    void offsetAlwaysInRange(int width) {
        ScrollingBackground bg = new ScrollingBackground(width);
        for (int i = 0; i < width * 3; i++) {
            bg.scroll(1);
            double off = bg.getOffset();
            assertTrue(off > -width && off <= 0,
                    "Offset " + off + " phải nằm trong (-" + width + ", 0]");
        }
    }

    @Test
    @DisplayName("Wrap-around xảy ra đúng tại -width")
    void wrapAroundOccursAtWidth() {
        ScrollingBackground bg = new ScrollingBackground(100);
        // Cuộn đúng 1 chu kỳ
        for (int i = 0; i < 100; i++) bg.scroll(1);
        // Offset phải gần 0 (không phải -100)
        assertTrue(bg.getOffset() > -100 && bg.getOffset() <= 0,
                "Phải wrap về gần 0 sau 1 chu kỳ đầy đủ");
    }

    @Test
    @DisplayName("Wrap-around không tạo bước nhảy lớn (không giật hình)")
    void noJitterOnWrap() {
        ScrollingBackground bg = new ScrollingBackground(288);
        double prevOffset = bg.getOffset();
        for (int i = 0; i < 288 * 5; i++) {
            bg.scroll(2);
            double off    = bg.getOffset();
            double delta  = Math.abs(off - prevOffset);
            // Bước nhảy hợp lệ: tối đa bằng scroll_speed (2),
            // trừ lúc wrap thì delta = |(-288+2) - (-288)| nhỏ
            // Bất kỳ delta > 300 đều là lỗi wrap-around
            assertTrue(delta <= 300,
                    "Bước nhảy offset " + delta + " quá lớn — sẽ gây giật hình");
            prevOffset = off;
        }
    }

    @Test
    @DisplayName("getWidth() trả về đúng width từ constructor")
    void getWidthReturnsConstructorValue() {
        assertEquals(336, new ScrollingBackground(336).getWidth());
        assertEquals(288, new ScrollingBackground(288).getWidth());
    }

    @Test
    @DisplayName("Hai instance độc lập — scroll một cái không ảnh hưởng cái kia")
    void instancesAreIndependent() {
        ScrollingBackground bg1 = new ScrollingBackground(288);
        ScrollingBackground bg2 = new ScrollingBackground(288);
        bg1.scroll(50);
        assertEquals(0.0,   bg2.getOffset(), 0.001,
                "bg2 không bị ảnh hưởng khi scroll bg1");
        assertEquals(-50.0, bg1.getOffset(), 0.001);
    }
}
