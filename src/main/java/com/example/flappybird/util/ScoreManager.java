package com.example.flappybird.util;

import java.io.*;
import java.nio.file.*;

/**
 * Handles persistence of the best score to disk.
 */
public final class ScoreManager {

    private ScoreManager() {}

    public static int loadBestScore() {
        Path p = Paths.get(GameConstants.SCORE_FILE);
        if (!Files.exists(p)) return 0;
        try (BufferedReader br = new BufferedReader(new FileReader(p.toFile()))) {
            String line = br.readLine();
            if (line != null) return Integer.parseInt(line.trim());
        } catch (Exception e) {
            System.err.println("[ScoreManager] Could not read score: " + e.getMessage());
        }
        return 0;
    }

    public static void saveBestScore(int score) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(GameConstants.SCORE_FILE))) {
            pw.print(score);
        } catch (Exception e) {
            System.err.println("[ScoreManager] Could not save score: " + e.getMessage());
        }
    }
}
