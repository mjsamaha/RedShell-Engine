package com.lobsterchops.redshell.rendering;

import com.lobsterchops.redshell.rendering.viewport.ViewportPolicy;

/** Immutable rendering setup: fixed world size, how it maps to the window, and the clear color. */
public final class RenderConfig {

    public static final String REGISTRY_NAME = "render";

    private final float worldWidth;
    private final float worldHeight;
    private final ViewportPolicy viewportPolicy;
    private final float clearRed;
    private final float clearGreen;
    private final float clearBlue;
    private final float clearAlpha;

    public RenderConfig(float worldWidth, float worldHeight, ViewportPolicy viewportPolicy) {
        this(worldWidth, worldHeight, viewportPolicy, 0.1f, 0.1f, 0.12f, 1f);
    }

    private RenderConfig(float worldWidth, float worldHeight, ViewportPolicy viewportPolicy,
                         float clearRed, float clearGreen, float clearBlue, float clearAlpha) {
        if (worldWidth <= 0f || worldHeight <= 0f) {
            throw new IllegalArgumentException("World size must be positive: " + worldWidth + "x" + worldHeight);
        }
        if (viewportPolicy == null) {
            throw new IllegalArgumentException("viewportPolicy must not be null");
        }
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.viewportPolicy = viewportPolicy;
        this.clearRed = clearRed;
        this.clearGreen = clearGreen;
        this.clearBlue = clearBlue;
        this.clearAlpha = clearAlpha;
    }

    public static RenderConfig defaults() {
        return new RenderConfig(640f, 360f, ViewportPolicy.FIT);
    }

    public RenderConfig withClearColor(float red, float green, float blue, float alpha) {
        return new RenderConfig(worldWidth, worldHeight, viewportPolicy, red, green, blue, alpha);
    }

    public float worldWidth() {
        return worldWidth;
    }

    public float worldHeight() {
        return worldHeight;
    }

    public ViewportPolicy viewportPolicy() {
        return viewportPolicy;
    }

    public float clearRed() {
        return clearRed;
    }

    public float clearGreen() {
        return clearGreen;
    }

    public float clearBlue() {
        return clearBlue;
    }

    public float clearAlpha() {
        return clearAlpha;
    }
}
