package com.example.flappybird.view;

import com.example.flappybird.util.ResourceLoader;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * VIEW helper — Quản lý toàn bộ audio playback.
 *
 * Thay đổi so với phiên bản cũ:
 *  - Dùng Sprite enum thay vì SpriteSheet String constants
 *  - Thêm playPoint() được gọi từ GameController khi SCORE_UPDATED
 *
 * Design Pattern: Singleton.
 */
public final class AudioManager {

    private static AudioManager instance;

    private MediaPlayer musicPlayer;
    private AudioClip   sfxClick, sfxFlap, sfxDead, sfxPoint;
    private boolean     soundEnabled = true;

    private AudioManager() {}

    public static AudioManager getInstance() {
        if (instance == null) instance = new AudioManager();
        return instance;
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    public void loadAll() {
        ResourceLoader rl = ResourceLoader.getInstance();

        sfxClick = rl.loadAudioClip(Sprite.SFX_CLICK.path);
        sfxFlap  = rl.loadAudioClip(Sprite.SFX_FLAP.path);
        sfxDead  = rl.loadAudioClip(Sprite.SFX_DEAD.path);
        sfxPoint = rl.loadAudioClip(Sprite.SFX_POINT.path);

        Media music = rl.loadMedia(Sprite.MUSIC.path);
        if (music != null) {
            musicPlayer = new MediaPlayer(music);
            musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        }
    }

    // ── Playback ──────────────────────────────────────────────────────────────

    public void playMusic() {
        if (musicPlayer == null || !soundEnabled) return;
        MediaPlayer.Status status = musicPlayer.getStatus();
        if (status != MediaPlayer.Status.PLAYING) {
            musicPlayer.play();
        }
    }

    public void pauseMusic() {
        if (musicPlayer != null) musicPlayer.pause();
    }

    public void playClick() { play(sfxClick); }
    public void playFlap()  { play(sfxFlap);  }
    public void playDead()  { play(sfxDead);  }
    public void playPoint() { play(sfxPoint); }

    private void play(AudioClip clip) {
        if (clip != null && soundEnabled) clip.play();
    }

    // ── Toggle ────────────────────────────────────────────────────────────────

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        if (!enabled) pauseMusic();
        else          playMusic();
    }

    public boolean isSoundEnabled() { return soundEnabled; }

    public void dispose() {
        if (musicPlayer != null) musicPlayer.dispose();
    }
}