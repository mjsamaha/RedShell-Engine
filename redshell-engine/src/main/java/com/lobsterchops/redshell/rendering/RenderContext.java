package com.lobsterchops.redshell.rendering;

import com.lobsterchops.redshell.rendering.camera.CoordinateConverter;
import com.lobsterchops.redshell.rendering.camera.GameCamera;

/** Per-frame rendering state handed to systems that draw. */
public final class RenderContext {

    private final Renderer2D renderer;
    private final GameCamera camera;
    private final CoordinateConverter coordinates;
    private float deltaTime;

    RenderContext(Renderer2D renderer, GameCamera camera, CoordinateConverter coordinates) {
        this.renderer = renderer;
        this.camera = camera;
        this.coordinates = coordinates;
    }

    void setDeltaTime(float deltaTime) {
        this.deltaTime = deltaTime;
    }

    public Renderer2D renderer() {
        return renderer;
    }

    public GameCamera camera() {
        return camera;
    }

    public CoordinateConverter coordinates() {
        return coordinates;
    }

    /** Game delta time of the frame being rendered. */
    public float deltaTime() {
        return deltaTime;
    }
}
