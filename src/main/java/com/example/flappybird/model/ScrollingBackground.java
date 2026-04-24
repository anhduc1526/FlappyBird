package com.example.flappybird.model;

/**
 * MODEL — Scrolling background strip.
 *
 * Giữ offset âm liên tục trong khoảng [-width, 0).
 * Renderer vẽ 2 bản sao liền kề: bản 1 tại offset, bản 2 tại offset + width.
 */
public class ScrollingBackground {

    private double offset = 0;
    private final int width;

    public ScrollingBackground(int textureWidth) {
        this.width  = textureWidth;
        this.offset = 0; // bắt đầu từ 0, scroll sang trái (âm dần)
    }

    /**
     * Dịch chuyển sang trái mỗi tick.
     * offset luôn nằm trong [-width, 0) — không bao giờ nhảy.
     */
    public void scroll(double distance) {
        offset -= distance;
        if (offset <= -width) {
            offset += width; // giữ phần dư, không reset cứng → không giật
        }
    }

    public double getOffset() { return offset; }
    public int    getWidth()  { return width; }
}