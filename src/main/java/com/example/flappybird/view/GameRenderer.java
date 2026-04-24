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
 * VIEW — All rendering logic (Canvas / GraphicsContext).
 */
public class GameRenderer {

    private final GraphicsContext gc;
    private final ResourceLoader  rl = ResourceLoader.getInstance();

    private final Image[][] birdFrames; // [type 0-2][0=up, 1=mid, 2=down]
    private final Image pipeUpper, pipeLower;
    private final Image bgDay, bgNight, land;
    private final Image message, gameOver;
    private final Image pauseBtn, pauseTab, resume, replay, button;
    private final Image arrowLeft, arrowRight;
    private final Image soundOn, soundOff;
    private final Image[] largeNums, smallNums, medals;

    public GameRenderer(GraphicsContext gc) {
        this.gc = gc;

        birdFrames = new Image[3][3];
        for (int t = 0; t < 3; t++) {
            birdFrames[t][0] = rl.loadImage(SpriteSheet.BIRD_UP[t]);
            birdFrames[t][1] = rl.loadImage(SpriteSheet.BIRD_MID[t]);
            birdFrames[t][2] = rl.loadImage(SpriteSheet.BIRD_DOWN[t]);
        }

        pipeUpper  = rl.loadImage(SpriteSheet.PIPE_UPPER);
        pipeLower  = rl.loadImage(SpriteSheet.PIPE_LOWER);
        bgDay      = rl.loadImage(SpriteSheet.BG_DAY);
        bgNight    = rl.loadImage(SpriteSheet.BG_NIGHT);
        land       = rl.loadImage(SpriteSheet.LAND);
        message    = rl.loadImage(SpriteSheet.MESSAGE);
        gameOver   = rl.loadImage(SpriteSheet.GAMEOVER);
        pauseBtn   = rl.loadImage(SpriteSheet.PAUSE_BTN);
        pauseTab   = rl.loadImage(SpriteSheet.PAUSE_TAB);
        resume     = rl.loadImage(SpriteSheet.RESUME);
        replay     = rl.loadImage(SpriteSheet.REPLAY);
        button     = rl.loadImage(SpriteSheet.BUTTON);
        arrowLeft  = rl.loadImage(SpriteSheet.ARROW_L);
        arrowRight = rl.loadImage(SpriteSheet.ARROW_R);
        soundOn    = rl.loadImage(SpriteSheet.SOUND_ON);
        soundOff   = rl.loadImage(SpriteSheet.SOUND_OFF);

        largeNums = new Image[10];
        smallNums = new Image[10];
        for (int i = 0; i < 10; i++) {
            largeNums[i] = rl.loadImage(SpriteSheet.largeNum(i));
            smallNums[i] = rl.loadImage(SpriteSheet.smallNum(i));
        }

        medals = new Image[3];
        for (int i = 0; i < 3; i++) {
            medals[i] = rl.loadImage(SpriteSheet.medal(i));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public render methods
    // ─────────────────────────────────────────────────────────────────────────

    public void clear() {
        gc.clearRect(0, 0, GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT);
    }

    // ── Backgrounds ───────────────────────────────────────────────────────────

    public void drawBackground(ScrollingBackground bg, boolean dayMode) {
        drawScrolling(dayMode ? bgDay : bgNight, bg, 0);
    }

    public void drawLand(ScrollingBackground landBg) {
        drawScrolling(land, landBg, GameConstants.LAND_Y);
    }

    /**
     * Vẽ 2 bản sao ảnh liền kề, không overlap, không gap.
     *
     * offset luôn trong [-width, 0):
     *   - Bản 1 tại x = offset          (đang rời khỏi màn hình bên trái)
     *   - Bản 2 tại x = offset + width  (tiếp nối ngay bên phải bản 1)
     *
     * Khi offset = 0:   bản1 tại 0, bản2 tại width  → bản1 lấp đầy màn hình
     * Khi offset = -w/2: bản1 tại -w/2, bản2 tại w/2 → mỗi bản lấp nửa màn hình
     * Khi offset → -width: bản1 sắp ra khỏi trái, bản2 gần lấp hết → rồi reset
     */
    private void drawScrolling(Image img, ScrollingBackground bg, int y) {
        double off = bg.getOffset();
        gc.drawImage(img, off,         y);  // bản đang trượt ra trái
        gc.drawImage(img, off + bg.getWidth(), y);  // bản tiếp nối bên phải
    }

    // ── Pipes ─────────────────────────────────────────────────────────────────

    public void drawPipes(List<PipeModel> pipes) {
        for (PipeModel p : pipes) {
            gc.drawImage(pipeUpper, p.getX(), p.getUpperY());
            gc.drawImage(pipeLower, p.getX(), p.getLowerY());
        }
    }

    // ── Bird ─────────────────────────────────────────────────────────────────

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

    /**
     * Renders an image rotated around its centre.
     * Mirrors SDL_RenderCopyEx from the original C++ project.
     */
    private void drawRotated(Image img, double x, double y, double angleDeg) {
        double w  = img.getWidth();
        double h  = img.getHeight();
        double cx = x + w / 2;
        double cy = y + h / 2;

        gc.save();
        gc.transform(new Affine(new Rotate(angleDeg, cx, cy)));
        gc.drawImage(img, x, y);
        gc.restore();
    }

    // ── HUD ───────────────────────────────────────────────────────────────────

    public void drawPauseButton() {
        gc.drawImage(pauseBtn, 320, 10);
    }

    public void drawLargeScore(int score) {
        String s      = String.valueOf(score);
        int    digitW = 30;
        int    posX   = (GameConstants.SCREEN_WIDTH - digitW * s.length()) / 2;
        for (char c : s.toCharArray()) {
            gc.drawImage(largeNums[c - '0'], posX, 10);
            posX += digitW;
        }
    }

    public void drawSmallScore(int score, int posY) {
        String s      = String.valueOf(score);
        int    digitW = 21;
        int    posX   = 271 - digitW * s.length();
        for (char c : s.toCharArray()) {
            gc.drawImage(smallNums[c - '0'], posX, posY);
            posX += digitW;
        }
    }

    public void drawSoundIcon(boolean on, int x, int y) {
        gc.drawImage(on ? soundOn : soundOff, x, y);
    }

    // ── Menu ──────────────────────────────────────────────────────────────────

    public void drawMenu(boolean soundEnabled) {
        int mx = (GameConstants.SCREEN_WIDTH  - GameConstants.MESSAGE_WIDTH)  / 2;
        int my = (GameConstants.SCREEN_HEIGHT - GameConstants.MESSAGE_HEIGHT - GameConstants.LAND_HEIGHT) / 2;
        gc.drawImage(message, mx, my);

        gc.drawImage(arrowLeft,  31, 317);
        gc.drawImage(arrowRight, 90, 317);
        gc.drawImage(arrowLeft,  0,  GameConstants.SCREEN_HEIGHT / 2.0);
        gc.drawImage(arrowRight, GameConstants.SCREEN_WIDTH - 13, GameConstants.SCREEN_HEIGHT / 2.0);

        drawSoundIcon(soundEnabled, 10, 10);
    }

    // ── Game-Over panel ───────────────────────────────────────────────────────

    public void drawGameOverPanel(int score, int bestScore, int medalIndex, double slideOffset) {
        int goX = (GameConstants.SCREEN_WIDTH  - GameConstants.GAMEOVER_WIDTH)  / 2;
        int goY = (500 - GameConstants.GAMEOVER_HEIGHT) / 2;

        gc.drawImage(gameOver, goX, goY + slideOffset);

        if (slideOffset < 1) {
            if (medalIndex >= 0) gc.drawImage(medals[medalIndex], 75, 263);
            drawSmallScore(score,     263);
            drawSmallScore(bestScore, 313);
            gc.drawImage(replay, (GameConstants.SCREEN_WIDTH - 100) / 2, 360);
        }
    }

    // ── Pause overlay ─────────────────────────────────────────────────────────

    public void drawPauseOverlay(int score, int bestScore, boolean soundEnabled) {
        gc.drawImage(pauseTab, (GameConstants.SCREEN_WIDTH - 250) / 2, (GameConstants.SCREEN_HEIGHT - 128) / 2 - 17);
        gc.drawImage(resume,   (GameConstants.SCREEN_WIDTH -  26) / 2, 365);
        gc.drawImage(button,   105, 316);
        drawSoundIcon(soundEnabled, 105, 266);
        drawSmallScore(score,     263);
        drawSmallScore(bestScore, 313);
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public Image getArrowLeft()  { return arrowLeft; }
    public Image getArrowRight() { return arrowRight; }
    public Image getSoundOn()    { return soundOn; }
    public Image getSoundOff()   { return soundOff; }
    public Image getReplay()     { return replay; }
    public Image getResume()     { return resume; }
    public Image getButton()     { return button; }
}