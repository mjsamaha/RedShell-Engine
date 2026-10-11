package com.lobsterchops.redshell.physics.collision;

/** Circle described by its centre and radius. */
public final class Circle implements Shape {

    private float x;
    private float y;
    private float radius;

    public Circle(float centerX, float centerY, float radius) {
        this.x = centerX;
        this.y = centerY;
        this.radius = radius;
    }

    public float radius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
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
        return x - radius;
    }

    @Override
    public float minY() {
        return y - radius;
    }

    @Override
    public float maxX() {
        return x + radius;
    }

    @Override
    public float maxY() {
        return y + radius;
    }
}