package com.lobsterchops.redshell.rendering.camera;

/** Moves a {@link GameCamera} to follow a target, optionally smoothed and kept inside world bounds. */
public final class CameraController {

    private final GameCamera camera;
    private final float viewWidth;
    private final float viewHeight;
    private float smoothing;
    private boolean bounded;
    private float minX;
    private float minY;
    private float maxX;
    private float maxY;

    /** {@code viewWidth} and {@code viewHeight} are the visible world size at zoom 1. */
    public CameraController(GameCamera camera, float viewWidth, float viewHeight) {
        this.camera = camera;
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
    }

    /** Restricts the camera so the view stays inside the rectangle. A smaller world is centered. */
    public void setBounds(float minX, float minY, float maxX, float maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.bounded = true;
    }

    public void clearBounds() {
        bounded = false;
    }

    /** 0 snaps to the target; larger values follow more tightly (units of 1/second). */
    public void setSmoothing(float smoothing) {
        if (smoothing < 0f) {
            throw new IllegalArgumentException("smoothing must not be negative: " + smoothing);
        }
        this.smoothing = smoothing;
    }

    public void snapTo(float targetX, float targetY) {
        camera.setPosition(clampX(targetX), clampY(targetY));
    }

    public void follow(float targetX, float targetY, float deltaSeconds) {
        float goalX = clampX(targetX);
        float goalY = clampY(targetY);
        if (smoothing <= 0f) {
            camera.setPosition(goalX, goalY);
            return;
        }
        float t = 1f - (float) Math.exp(-smoothing * deltaSeconds);
        camera.setPosition(camera.x() + (goalX - camera.x()) * t, camera.y() + (goalY - camera.y()) * t);
    }

    private float clampX(float x) {
        return bounded ? clamp(x, minX, maxX, viewWidth * camera.zoom() / 2f) : x;
    }

    private float clampY(float y) {
        return bounded ? clamp(y, minY, maxY, viewHeight * camera.zoom() / 2f) : y;
    }

    private static float clamp(float value, float min, float max, float half) {
        if (max - min <= half * 2f) {
            return (min + max) / 2f;
        }
        return Math.max(min + half, Math.min(max - half, value));
    }
}