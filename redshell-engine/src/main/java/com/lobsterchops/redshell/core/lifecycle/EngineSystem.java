package com.lobsterchops.redshell.core.lifecycle;

import com.lobsterchops.redshell.core.context.EngineContext;

/**
 * The one lifecycle shape for everything engine-managed. All methods are optional.
 * Systems are initialized in registration order and disposed in reverse order.
 */
public interface EngineSystem {

    default void initialize(EngineContext context) {
    }

    /** Called zero or more times per frame with a constant step, only when fixed timestep is enabled. */
    default void fixedUpdate(float fixedDeltaTime) {
    }

    default void update(float deltaTime) {
    }

    default void render(float deltaTime) {
    }

    default void resize(int width, int height) {
    }

    /** The application lost focus or was hidden (for example a browser tab switch). */
    default void pause() {
    }

    default void resume() {
    }

    default void dispose() {
    }
}
