package com.lobsterchops.redshell.rendering.camera;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

/** Converts between screen (pixel, y-down) coordinates and world coordinates for the current viewport. */
public final class CoordinateConverter {

    private final Viewport viewport;

    public CoordinateConverter(Viewport viewport) {
        this.viewport = viewport;
    }

    /** Writes the world position of a screen point into {@code out} and returns it. */
    public Vector2 screenToWorld(float screenX, float screenY, Vector2 out) {
        out.set(screenX, screenY);
        return viewport.unproject(out);
    }

    /** Writes the screen position of a world point into {@code out} and returns it. */
    public Vector2 worldToScreen(float worldX, float worldY, Vector2 out) {
        out.set(worldX, worldY);
        return viewport.project(out);
    }
}
