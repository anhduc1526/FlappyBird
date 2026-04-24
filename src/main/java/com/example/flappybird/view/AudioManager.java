package com.example.flappybird.view;

import com.example.flappybird.util.ResourceLoader;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * VIEW helper — centralises all audio playback.
 *
 * Mirrors Graphics::loadSound / playSound / loadMusic / playMusic / pauseMusic
 * from the original C++ graphics.cpp.
 *
 * Design Pattern: Singleton.
 */
public final class AudioManager {

    private static AudioManager instance;

    private MediaPlayer  musicPlayer;
    private AudioClip    sfxClick, sfxFlap, sfxDead, sfxPoint;
    private boolean      soundEnabled = true;

    private AudioManager() {}

    public static AudioManager getInstance() {
        if (instance == null) instance = new AudioManager();
        return instance;
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    public void loadAll() {
        ResourceLoader rl = ResourceLoader.getInstance();

        sfxClick = rl.loadAudioClip(SpriteSheet.SFX_CLICK);
        sfxFlap  = rl.loadAudioClip(SpriteSheet.SFX_FLAP);
        sfxDead  = rl.loadAudioClip(SpriteSheet.SFX_DEAD);
        sfxPoint = rl.loadAudioClip(SpriteSheet.SFX_POINT);

        Media music = rl.loadMedia(SpriteSheet.MUSIC);
        if (music != null) {
            musicPlayer = new MediaPlayer(music);
            musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        }
    }

    // ── Playback ──────────────────────────────────────────────────────────────

    public void playMusic() {
        if (musicPlayer == null || !soundEnabled) return;
        if (musicPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
            musicPlayer.play();
        } else if (musicPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
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
