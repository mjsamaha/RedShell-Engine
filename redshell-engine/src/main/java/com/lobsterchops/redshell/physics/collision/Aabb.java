package com.lobsterchops.redshell.physics.collision;

/** Axis-aligned box described by its centre and half extents. */
public final class Aabb implements Shape {

    private float x;
    private float y;
    private float halfWidth;
    private float halfHeight;

    public Aabb(float centerX, float centerY, float width, float height) {
        this.x = centerX;
        this.y = centerY;
        this.halfWidth = width / 2f;
        this.halfHeight = height / 2f;
    }

    public float halfWidth() {
        return halfWidth;
    }

    public float halfHeight() {
        return halfHeight;
    }

    public void setSize(float width, float height) {
        this.halfWidth = width / 2f;
        this.halfHeight = height / 2f;
    }

    @Override
    public float centerX() {
        return x;
    }

    @Override
    public float centerY() {
        return y;
    }

    @Override
    public void setCenter(float centerX, float centerY) {
        this.x = centerX;
        this.y = centerY;
    }

    @Override
    public float minX() {
        return x - halfWidth;
    }

    @Override
    public float minY() {
        return y - halfHeight;
    }

    @Override
    public float maxX() {
        return x + halfWidth;
    }

    @Override
    public float maxY() {
        return y + halfHeight;
    }
}