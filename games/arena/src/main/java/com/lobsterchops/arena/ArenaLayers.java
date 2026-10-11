package com.lobsterchops.arena;

import com.lobsterchops.redshell.physics.layer.CollisionMatrix;

/** Collision layers: only bullets-vs-enemies and enemies-vs-player matter. */
final class ArenaLayers {

    static final int PLAYER = 0;
    static final int ENEMY = 1;
    static final int BULLET = 2;

    private ArenaLayers() {
    }

    static CollisionMatrix matrix() {
        return new CollisionMatrix()
                .setCollides(BULLET, PLAYER, false)
                .setCollides(BULLET, BULLET, false)
                .setCollides(ENEMY, ENEMY, false)
                .setCollides(PLAYER, PLAYER, false);
    }
}