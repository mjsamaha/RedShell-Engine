package com.lobsterchops.arena;

/** Arena size and tuning values shared by the gameplay systems. */
final class ArenaConfig {

    static final float WIDTH = 640f;
    static final float HEIGHT = 360f;
    static final float PLAYER_SPEED = 120f;
    static final float BULLET_SPEED = 280f;
    static final float FIRE_INTERVAL = 0.22f;
    static final int LIVES = 3;

    private ArenaConfig() {
    }
}