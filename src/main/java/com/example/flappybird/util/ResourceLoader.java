package com.example.flappybird.util;

import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton resource loader — caches images and audio clips.
 * Design Pattern: Singleton + Flyweight (cache).
 */
public final class ResourceLoader {

    private static ResourceLoader instance;
    private final Map<String, Image> imageCache = new HashMap<>();

    private ResourceLoader() {}

    public static ResourceLoader getInstance() {
        if (instance == null) instance = new ResourceLoader();
        return instance;
    }

    /**
     * Load an image from the resources folder.
     * Path is relative to /com/flappybird/ in resources.
     */
    public Image loadImage(String resourcePath) {
        return imageCache.computeIfAbsent(resourcePath, path -> {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                System.err.println("[ResourceLoader] Missing: " + path);
                // Return a tiny placeholder so the game doesn't crash
                return createPlaceholder();
            }
            return new Image(is);
        });
    }

    /** Load an AudioClip (short SFX). */
    public AudioClip loadAudioClip(String resourcePath) {
        URL url = getClass().getResource(resourcePath);
        if (url == null) {
            System.err.println("[ResourceLoader] Missing audio: " + resourcePath);
            return null;
        }
        return new AudioClip(url.toExternalForm());
    }

    /** Load a Media object for background music. */
    public Media loadMedia(String resourcePath) {
        URL url = getClass().getResource(resourcePath);
        if (url == null) {
            System.err.println("[ResourceLoader] Missing music: " + resourcePath);
            return null;
        }
        return new Media(url.toExternalForm());
    }

    private Image createPlaceholder() {
        // 1×1 transparent pixel
        return new javafx.scene.image.WritableImage(1, 1);
    }

    public void clearCache() {
        imageCache.clear();
    }
}
