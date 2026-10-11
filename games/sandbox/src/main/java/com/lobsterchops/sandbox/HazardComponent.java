package com.lobsterchops.sandbox;

import com.lobsterchops.redshell.entity.component.Component;

/** Marks an entity that hurts the player and patrols horizontally between two x positions. */
final class HazardComponent implements Component {

    final float minX;
    final float maxX;
    final float speed;

    HazardComponent(float minX, float maxX, float speed) {
        this.minX = minX;
        this.maxX = maxX;
        this.speed = speed;
    }
}