package com.example.flappybird.view;

import com.example.flappybird.model.*;
import com.example.flappybird.util.GameConstants;
import com.example.flappybird.util.ResourceLoader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;

import java.util.List;

/**
 * VIEW — Toàn bộ logic render lên Canvas / GraphicsContext.
 *
 * Thay đổi so với phiên bản cũ:
 *  - Dùng Sprite enum thay vì SpriteSheet String → type-safe
 *  - Tất cả toạ độ HUD lấy từ HudLayout thay vì magic numbers
 *  - Bỏ các getter getArrowLeft(), getReplay(),... (HudLayout đã đủ thông tin)
 *
 * Design Pattern: Passive View — chỉ vẽ, không giữ state game.
 */
public class GameRenderer {

    private final GraphicsContext gc;
    private final ResourceLoader  rl = ResourceLoader.getInstance();

    // Bird frames: [type 0-2][0=up, 1=mid, 2=down]
    private final Image[][] birdFrames;
    private final Image pipeUpper, pipeLower;
    private final Image bgDay, bgNight, landImg;
    private final Image message, gameOver;
    private final Image pauseBtn, pauseTab, resume, replay, button;
    private final Image arrowLeft, arrowRight;
    private final Image soundOn, soundOff;
    private final Image[] largeNums, smallNums, medals;

    public GameRenderer(GraphicsContext gc) {
        this.gc = gc;

        birdFrames = new Image[3][3];
        for (int t = 0; t < 3; t++) {
            birdFrames[t][0] = rl.loadImage(Sprite.birdUp(t));
            birdFrames[t][1] = rl.loadImage(Sprite.birdMid(t));
            birdFrames[t][2] = rl.loadImage(Sprite.birdDown(t));
        }

        pipeUpper  = rl.loadImage(Sprite.PIPE_UPPER.path);
        pipeLower  = rl.loadImage(Sprite.PIPE_LOWER.path);
        bgDay      = rl.loadImage(Sprite.BG_DAY.path);
        bgNight    = rl.loadImage(Sprite.BG_NIGHT.path);
        landImg    = rl.loadImage(Sprite.LAND.path);
        message    = rl.loadImage(Sprite.MESSAGE.path);
        gameOver   = rl.loadImage(Sprite.GAMEOVER.path);
        pauseBtn   = rl.loadImage(Sprite.PAUSE_BTN.path);
        pauseTab   = rl.loadImage(Sprite.PAUSE_TAB.path);
        resume     = rl.loadImage(Sprite.RESUME.path);
        replay     = rl.loadImage(Sprite.REPLAY.path);
        button     = rl.loadImage(Sprite.BUTTON.path);
        arrowLeft  = rl.loadImage(Sprite.ARROW_L.path);
        arrowRight = rl.loadImage(Sprite.ARROW_R.path);
        soundOn    = rl.loadImage(Sprite.SOUND_ON.path);
        soundOff   = rl.loadImage(Sprite.SOUND_OFF.path);

        largeNums = new Image[10];
        smallNums = new Image[10];
        for (int i = 0; i < 10; i++) {
            largeNums[i] = rl.loadImage(Sprite.largeNum(i));
            smallNums[i] = rl.loadImage(Sprite.smallNum(i));
        }

        medals = new Image[3];
        for (int i = 0; i < 3; i++) {
            medals[i] = rl.loadImage(Sprite.medal(i));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public render methods
    // ─────────────────────────────────────────────────────────────────────────

    public void clear() {
        gc.clearRect(0, 0, GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT);
    }

    // ── Backgrounds ───────────────────────────────────────────────────────────

    public void drawBackground(ScrollingBackground bg, boolean isDay) {
        drawScrolling(isDay ? bgDay : bgNight, bg, 0);
    }

    public void drawLand(ScrollingBackground landBg) {
        drawScrolling(landImg, landBg, GameConstants.LAND_Y);
    }

    /**
     * Vẽ 2 bản sao ảnh liền kề, không overlap, không gap.
     * offset ∈ [-width, 0): bản1 tại offset, bản2 tại offset + width.
     */
    private void drawScrolling(Image img, ScrollingBackground bg, int y) {
        double off = bg.getOffset();
        gc.drawImage(img, off,                y);
        gc.drawImage(img, off + bg.getWidth(), y);
    }

    // ── Pipes ─────────────────────────────────────────────────────────────────

    public void drawPipes(List<PipeModel> pipes) {
        for (PipeModel p : pipes) {
            gc.drawImage(pipeUpper, p.getX(), p.getUpperY());
            gc.drawImage(pipeLower, p.getX(), p.getLowerY());
        }
    }

    // ── Bird ──────────────────────────────────────────────────────────────────

    public void drawBird(BirdModel bird) {
        bird.updateAnimation();
        int frame = getBirdFrameIndex(bird.getAnimationFrame());
        Image img = birdFrames[bird.getTypeBird()][frame];
        drawRotated(img, bird.getX(), bird.getY(), bird.getAngle());
    }

    private int getBirdFrameIndex(int animFrame) {
        if (animFrame < GameConstants.ANIM_UP_END)  return 0;
        if (animFrame < GameConstants.ANIM_MID_END) return 1;
        return 2;
    }

    private void drawRotated(Image img, double x, double y, double angleDeg) {
        double cx = x + img.getWidth()  / 2;
        double cy = y + img.getHeight() / 2;
        gc.save();
        gc.transform(new Affine(new Rotate(angleDeg, cx, cy)));
        gc.drawImage(img, x, y);
        gc.restore();
    }

    // ── HUD ───────────────────────────────────────────────────────────────────

    public void drawPauseButton() {
        gc.drawImage(pauseBtn, HudLayout.PAUSE_BTN_X, HudLayout.PAUSE_BTN_Y);
    }

    public void drawLargeScore(int score) {
        String s    = String.valueOf(score);
        int    posX = (GameConstants.SCREEN_WIDTH - HudLayout.LARGE_SCORE_DIGIT_W * s.length()) / 2;
        for (char c : s.toCharArray()) {
            gc.drawImage(largeNums[c - '0'], posX, HudLayout.LARGE_SCORE_Y);
            posX += HudLayout.LARGE_SCORE_DIGIT_W;
        }
    }

    public void drawSmallScore(int score, int posY) {
        String s    = String.valueOf(score);
        int    posX = HudLayout.SMALL_SCORE_RIGHT_X - HudLayout.SMALL_SCORE_DIGIT_W * s.length();
        for (char c : s.toCharArray()) {
            gc.drawImage(smallNums[c - '0'], posX, posY);
            posX += HudLayout.SMALL_SCORE_DIGIT_W;
        }
    }

    public void drawSoundIcon(boolean on, int x, int y) {
        gc.drawImage(on ? soundOn : soundOff, x, y);
    }

    // ── Menu ──────────────────────────────────────────────────────────────────

    public void drawMenu(boolean soundEnabled) {
        int mx = (GameConstants.SCREEN_WIDTH  - GameConstants.MESSAGE_WIDTH)  / 2;
        int my = (GameConstants.SCREEN_HEIGHT - GameConstants.MESSAGE_HEIGHT
                - GameConstants.LAND_HEIGHT) / 2;
        gc.drawImage(message, mx, my);

        gc.drawImage(arrowLeft,  HudLayout.ARROW_LEFT_X,  HudLayout.ARROW_LEFT_Y);
        gc.drawImage(arrowRight, HudLayout.ARROW_RIGHT_X, HudLayout.ARROW_RIGHT_Y);
        gc.drawImage(arrowLeft,  HudLayout.DAY_ARROW_L_X, HudLayout.DAY_ARROW_Y);
        gc.drawImage(arrowRight, HudLayout.DAY_ARROW_R_X, HudLayout.DAY_ARROW_Y);

        drawSoundIcon(soundEnabled, HudLayout.SOUND_MENU_X, HudLayout.SOUND_MENU_Y);
    }

    // ── Game-Over panel ───────────────────────────────────────────────────────

    /**
     * @param slideOffset  giá trị ease-out (220→0); khi = 0 panel đã vào vị trí
     */
    public void drawGameOverPanel(int score, int bestScore, int medalIndex, double slideOffset) {
        gc.drawImage(gameOver, HudLayout.GAME_OVER_X, HudLayout.GAME_OVER_Y + slideOffset);

        if (slideOffset < 1) {
            if (medalIndex >= 0) gc.drawImage(medals[medalIndex], HudLayout.MEDAL_X, HudLayout.MEDAL_Y);
            drawSmallScore(score,     HudLayout.GO_SCORE_ROW_Y);
            drawSmallScore(bestScore, HudLayout.GO_BEST_ROW_Y);
            gc.drawImage(replay, HudLayout.REPLAY_X, HudLayout.REPLAY_Y);
        }
    }

    // ── Pause overlay ─────────────────────────────────────────────────────────

    public void drawPauseOverlay(int score, int bestScore, boolean soundEnabled) {
        gc.drawImage(pauseTab, HudLayout.PAUSE_TAB_X,    HudLayout.PAUSE_TAB_Y);
        gc.drawImage(resume,   HudLayout.PAUSE_RESUME_X, HudLayout.PAUSE_RESUME_Y);
        gc.drawImage(button,   HudLayout.PAUSE_DAY_BTN_X, HudLayout.PAUSE_DAY_BTN_Y);
        drawSoundIcon(soundEnabled, HudLayout.PAUSE_SOUND_X, HudLayout.PAUSE_SOUND_Y);
        drawSmallScore(score,     HudLayout.PAUSE_SCORE_ROW_Y);
        drawSmallScore(bestScore, HudLayout.PAUSE_BEST_ROW_Y);
    }
}