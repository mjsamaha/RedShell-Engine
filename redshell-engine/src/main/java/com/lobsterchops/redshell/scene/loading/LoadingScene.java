package com.lobsterchops.redshell.scene.loading;

import com.badlogic.gdx.graphics.Color;
import com.lobsterchops.redshell.events.types.AssetsLoadedEvent;
import com.lobsterchops.redshell.rendering.Renderer2D;
import com.lobsterchops.redshell.scene.Scene;
import com.lobsterchops.redshell.scene.transition.Transition;

/**
 * Shows a progress bar while the target scene's asset group loads without blocking, then replaces the
 * whole stack with the target scene. The target takes over the loaded group's single reference.
 */
public final class LoadingScene extends Scene {

    private static final float BAR_WIDTH = 240f;
    private static final float BAR_HEIGHT = 12f;
    private static final Color TRACK = new Color(0.2f, 0.2f, 0.25f, 1f);
    private static final Color FILL = new Color(0.8f, 0.15f, 0.15f, 1f);

    private final Scene target;
    private final Transition after;
    private SceneLoader loader;
    private boolean finished;

    public LoadingScene(Scene target, Transition after) {
        this.target = target;
        this.after = after;
    }

    public Scene target() {
        return target;
    }

    public float progress() {
        return loader == null ? 0f : loader.progress();
    }

    @Override
    protected void onEnter() {
        loader = new SceneLoader(context().assets(), target.assets());
        if (context().renderer() != null) {
            Renderer2D renderer = context().renderer();
            renderer.camera().setZoom(1f);
            renderer.camera().setPosition(renderer.worldWidth() / 2f, renderer.worldHeight() / 2f);
        }
    }

    @Override
    protected void onUpdate(float deltaTime) {
        if (finished || !loader.step()) {
            return;
        }
        finished = true;
        loader.handOver();
        target.markAssetsAcquired();
        context().events().publish(new AssetsLoadedEvent(target.assets().name()));
        context().scenes().replaceAll(target, after);
    }

    @Override
    protected void onRender(float deltaTime) {
        Renderer2D renderer = context().renderer();
        if (renderer == null) {
            return;
        }
        float x = renderer.worldWidth() / 2f - BAR_WIDTH / 2f;
        float y = renderer.worldHeight() / 2f - BAR_HEIGHT / 2f;
        renderer.fillRect(x, y, BAR_WIDTH, BAR_HEIGHT, TRACK);
        renderer.fillRect(x, y, BAR_WIDTH * progress(), BAR_HEIGHT, FILL);
    }

    @Override
    protected void onDispose() {
        if (loader != null) {
            loader.cancel();
        }
    }
}