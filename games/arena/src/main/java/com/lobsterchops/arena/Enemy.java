package com.lobsterchops.arena;

import com.lobsterchops.redshell.entity.component.Component;

/** Marks a chaser. {@code dead} stops two bullets hitting in one frame from scoring twice. */
final class Enemy implements Component {
    final float speed;
    boolean dead;

    Enemy(float speed) {
        this.speed = speed;
    }
}