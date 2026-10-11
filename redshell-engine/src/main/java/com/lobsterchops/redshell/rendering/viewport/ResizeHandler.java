package com.lobsterchops.redshell.rendering.viewport;

import com.badlogic.gdx.utils.viewport.Viewport;

/** Applies viewport changes on window resize. Ignores zero-sized (minimized) windows. */
public final class ResizeHandler {

    private final Viewport viewport;
    private boolean cameraCentered;

    public ResizeHandler(Viewport viewport) {
        this.viewport = viewport;
    }

    /** Returns true if the viewport was updated. The camera is centered on the first resize only. */
    public boolean resize(int width, int height) {
        if (width <= 0 || height <= 0) {
            return false;
        }
        viewport.update(width, height, !cameraCentered);
        cameraCentered = true;
        return true;
    }
}
