package com.lobsterchops.redshell.rendering.viewport;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/** How the fixed world size is mapped onto the window. Wraps LibGDX viewports. */
public enum ViewportPolicy {
    /** Keeps the aspect ratio and adds black bars. */
    FIT {
        @Override
        public Viewport createViewport(Camera camera, float worldWidth, float worldHeight) {
            return new FitViewport(worldWidth, worldHeight, camera);
        }
    },
    /** Keeps the aspect ratio and crops what does not fit. */
    FILL {
        @Override
        public Viewport createViewport(Camera camera, float worldWidth, float worldHeight) {
            return new FillViewport(worldWidth, worldHeight, camera);
        }
    },
    /** Distorts the world to fill the window. */
    STRETCH {
        @Override
        public Viewport createViewport(Camera camera, float worldWidth, float worldHeight) {
            return new StretchViewport(worldWidth, worldHeight, camera);
        }
    },
    /** Keeps the aspect ratio and reveals extra world on the longer axis. */
    EXTEND {
        @Override
        public Viewport createViewport(Camera camera, float worldWidth, float worldHeight) {
            return new ExtendViewport(worldWidth, worldHeight, camera);
        }
    };

    public abstract Viewport createViewport(Camera camera, float worldWidth, float worldHeight);
}
