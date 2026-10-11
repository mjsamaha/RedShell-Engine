package com.lobsterchops.redshell.core;

import com.lobsterchops.redshell.core.context.EngineContext;
import com.lobsterchops.redshell.core.lifecycle.RenderBracket;
import com.lobsterchops.redshell.core.lifecycle.SystemRegistry;
import com.lobsterchops.redshell.core.time.FixedStepAccumulator;
import com.lobsterchops.redshell.debug.log.ExceptionReporter;

/**
 * Owns the system list, the context and the main update/render pass. The engine never touches
 * LibGDX: the application adapter feeds it the raw frame delta and lifecycle callbacks.
 *
 * <p>Frame flow: clock tick, optional fixed steps, update, then render between the render bracket's
 * begin and end. Unhandled exceptions are reported once with context and then rethrown (fail fast).
 */
public final class Engine {

    private static final String TAG = "Engine";

    private final EngineContext context;
    private final SystemRegistry systems;
    private final RenderBracket renderBracket;
    private final FixedStepAccumulator fixedSteps;
    private final ExceptionReporter exceptionReporter;
    private boolean started;
    private boolean disposed;

    Engine(EngineContext context, SystemRegistry systems, RenderBracket renderBracket) {
        this.context = context;
        this.systems = systems;
        this.renderBracket = renderBracket;
        this.exceptionReporter = new ExceptionReporter(context.log());
        this.fixedSteps = context.config().fixedTimestepEnabled()
                ? new FixedStepAccumulator(context.config().fixedStepSeconds(), context.config().maxFrameSeconds())
                : null;
    }

    public EngineContext context() {
        return context;
    }

    /** Initializes all systems in registration order. */
    public void start() {
        if (started) {
            throw new IllegalStateException("Engine already started");
        }
        started = true;
        try {
            systems.initializeAll(context);
        } catch (RuntimeException | Error e) {
            exceptionReporter.report("Engine.start", e);
            throw e;
        }
        context.log().info(TAG, "Started with " + systems.size() + " systems");
    }

    /** Runs one frame. {@code rawDeltaSeconds} is the real time since the previous frame. */
    public void frame(float rawDeltaSeconds) {
        requireRunning();
        try {
            context.clock().tick(rawDeltaSeconds);
            float delta = context.clock().deltaTime();
            update(delta);
            render(delta);
        } catch (RuntimeException | Error e) {
            exceptionReporter.report("Engine.frame", e);
            throw e;
        }
    }

    public void resize(int width, int height) {
        requireRunning();
        if (width <= 0 || height <= 0) {
            return;
        }
        systems.resizeAll(width, height);
    }

    public void pause() {
        requireRunning();
        context.log().debug(TAG, "Paused");
        systems.pauseAll();
    }

    public void resume() {
        requireRunning();
        context.log().debug(TAG, "Resumed");
        systems.resumeAll();
    }

    /** Disposes all systems in reverse order. Safe to call more than once. */
    public void shutdown() {
        if (disposed) {
            return;
        }
        disposed = true;
        systems.disposeAll();
        context.log().info(TAG, "Shut down");
    }

    private void update(float delta) {
        if (fixedSteps != null) {
            int steps = fixedSteps.advance(delta);
            for (int i = 0; i < steps; i++) {
                systems.fixedUpdateAll(fixedSteps.stepSeconds());
            }
        }
        systems.updateAll(delta);
    }

    private void render(float delta) {
        renderBracket.beginRender();
        try {
            systems.renderAll(delta);
        } finally {
            renderBracket.endRender();
        }
    }

    private void requireRunning() {
        if (!started || disposed) {
            throw new IllegalStateException("Engine is not running");
        }
    }
}
