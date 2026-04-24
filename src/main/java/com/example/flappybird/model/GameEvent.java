package com.example.flappybird.model;

/**
 * MODEL — Domain events emitted by GameModel.
 *
 * Cho phép Controller đăng ký lắng nghe thay vì polling getPhase() mỗi tick.
 * Design Pattern: Observer (lightweight callback variant).
 */
public enum GameEvent {

    /** Bird va chạm pipe hoặc mặt đất → bắt đầu death animation. */
    BIRD_DIED,

    /** Bird vượt qua pipe → score tăng 1. */
    SCORE_UPDATED,

    /** Game được reset về MENU sau khi nhấn Replay. */
    GAME_RESET,

    /** Chuyển từ PLAYING → PAUSED. */
    GAME_PAUSED,

    /** Chuyển từ PAUSED → PLAYING. */
    GAME_RESUMED,

    /** Sound được bật hoặc tắt. */
    SOUND_TOGGLED,

    /** Người dùng yêu cầu thoát ứng dụng. */
    QUIT_REQUESTED
}