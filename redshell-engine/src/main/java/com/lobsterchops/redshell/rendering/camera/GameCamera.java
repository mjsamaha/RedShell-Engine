package com.lobsterchops.redshell.rendering.camera;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;

/** The 2D game camera. Wraps LibGDX's {@link OrthographicCamera} so games never touch it directly. */
public final class GameCamera {

    private final OrthographicCamera camera = new OrthographicCamera();

    public void setPosition(float x, float y) {
        camera.position.set(x, y, 0f);
    }

    public void translate(float dx, float dy) {
        camera.translate(dx, dy);
    }

    public float x() {
        return camera.position.x;
    }

    public float y() {
        return camera.position.y;
    }

    public float zoom() {
        return camera.zoom;
    }

    public void setZoom(float zoom) {
        if (zoom <= 0f) {
            throw new IllegalArgumentException("zoom must be positive: " + zoom);
        }
        camera.zoom = zoom;
    }

    /** Recomputes the matrices. Called by the renderer at the start of each render pass. */
    public void update() {
        camera.update();
    }

    public Matrix4 combined() {
        return camera.combined;
    }

    /** The wrapped camera, for rendering adapters (viewports, batch). Game code should not need it. */
    public OrthographicCamera unwrap() {
        return camera;
    }
}
