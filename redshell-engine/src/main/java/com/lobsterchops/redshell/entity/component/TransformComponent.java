package com.lobsterchops.redshell.entity.component;

import com.lobsterchops.redshell.entity.Entity;

/** Position, rotation and scale. An optional parent offsets the position (translation only). */
public final class TransformComponent implements Component {

    public float x;
    public float y;
    public float rotationDegrees;
    public float scaleX = 1f;
    public float scaleY = 1f;
    public Entity parent;

    public TransformComponent() {
    }

    public TransformComponent(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float worldX() {
        TransformComponent parentTransform = parent == null ? null : parent.get(TransformComponent.class);
        return parentTransform == null ? x : x + parentTransform.worldX();
    }

    public float worldY() {
        TransformComponent parentTransform = parent == null ? null : parent.get(TransformComponent.class);
        return parentTransform == null ? y : y + parentTransform.worldY();
    }
}