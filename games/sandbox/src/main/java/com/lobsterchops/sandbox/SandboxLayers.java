package com.lobsterchops.sandbox;

import com.lobsterchops.redshell.physics.layer.CollisionMatrix;

/** Collision layer indices and which of them collide. */
public final class SandboxLayers {

    public static final int WALL = 0;
    public static final int PLAYER = 1;
    public static final int COIN = 2;
    public static final int HAZARD = 3;

    private SandboxLayers() {
    }

    public static CollisionMatrix matrix() {
        return new CollisionMatrix()
                .setCollides(COIN, WALL, false)
                .setCollides(COIN, COIN, false)
                .setCollides(COIN, HAZARD, false)
                .setCollides(HAZARD, WALL, false)
                .setCollides(HAZARD, HAZARD, false)
                .setCollides(WALL, WALL, false);
    }
}