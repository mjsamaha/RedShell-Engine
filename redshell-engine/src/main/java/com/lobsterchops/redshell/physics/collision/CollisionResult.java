package com.lobsterchops.redshell.physics.collision;

/**
 * Outcome of testing shape A against shape B. When colliding, moving A by {@code normal * depth}
 * separates the two shapes. Reusable: {@link Collisions#test} overwrites every field.
 */
public final class CollisionResult {

    private boolean colliding;
    private float normalX;
    private float normalY;
    private float depth;

    public boolean colliding() {
        return colliding;
    }

    public float normalX() {
        return normalX;
    }

    public float normalY() {
        return normalY;
    }

    public float depth() {
        return depth;
    }

    void set(boolean colliding, float normalX, float normalY, float depth) {
        this.colliding = colliding;
        this.normalX = normalX;
        this.normalY = normalY;
        this.depth = depth;
    }
}