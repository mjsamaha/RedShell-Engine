package com.lobsterchops.redshell.scene;

import com.lobsterchops.redshell.assets.catalog.AssetGroup;
import com.lobsterchops.redshell.ecs.World;
import com.lobsterchops.redshell.events.Event;
import com.lobsterchops.redshell.events.EventListener;
import com.lobsterchops.redshell.events.SubscriptionGroup;
import com.lobsterchops.redshell.input.context.InputContext;

/**
 * One screen of the game: a menu, a level, a pause overlay. Lifecycle: {@code onEnter}, then
 * {@code onUpdate}/{@code onRender} each frame, then {@code onExit}, then {@code onDispose}.
 *
 * <p>A scene owns its {@link World}, its event subscriptions, its input context and one reference to its
 * {@link #assets() asset group}. All of them are released when the scene is disposed.
 */
public abstract class Scene {

    private final SubscriptionGroup subscriptions = new SubscriptionGroup();
    private SceneContext context;
    private World world;
    private boolean assetsAcquired;
    private boolean entered;
    private boolean disposed;

    /** Display name, used in events and logs. */
    public String name() {
        return getClass().getSimpleName();
    }

    /** Assets this scene needs, or null. Loaded before the scene enters (see {@code SceneManager.load}). */
    public AssetGroup assets() {
        return null;
    }

    /** Input context active while this scene is on the stack, or null to leave input unchanged. */
    public InputContext inputContext() {
        return null;
    }

    /** What happens to the scene below while this one is on top. */
    public SceneFlags flags() {
        return SceneFlags.OPAQUE;
    }

    protected void onEnter() {
    }

    protected void onUpdate(float deltaTime) {
    }

    protected void onRender(float deltaTime) {
    }

    protected void onResize(int width, int height) {
    }

    protected void onPause() {
    }

    protected void onResume() {
    }

    protected void onExit() {
    }

    protected void onDispose() {
    }

    protected final SceneContext context() {
        return context;
    }

    protected final World world() {
        return world;
    }

    /** Subscribes to an event for the lifetime of this scene; the subscription is cancelled on dispose. */
    protected final <E extends Event> void subscribe(Class<E> type, EventListener<E> listener) {
        subscriptions.add(context.events().subscribe(type, listener));
    }

    /** Marks that the scene's asset group was already loaded (and referenced) on its behalf. */
    public final void markAssetsAcquired() {
        assetsAcquired = true;
    }

    public final boolean isAssetsAcquired() {
        return assetsAcquired;
    }

    public final boolean isDisposed() {
        return disposed;
    }

    /** Called by the scene stack. */
    public final void enter(SceneContext sceneContext) {
        if (entered) {
            throw new IllegalStateException(name() + " was already entered; scenes are single use");
        }
        entered = true;
        this.context = sceneContext;
        this.world = new World();
        AssetGroup group = assets();
        if (group != null && !assetsAcquired) {
            sceneContext.log().warn("Scene", name() + " entered without preloaded assets; loading synchronously");
            sceneContext.assets().load(group);
            sceneContext.assets().finishLoading();
            assetsAcquired = true;
        }
        InputContext inputContext = inputContext();
        if (inputContext != null) {
            sceneContext.input().contexts().push(inputContext);
        }
        onEnter();
    }

    /** Called by the scene stack. */
    public final void update(float deltaTime) {
        onUpdate(deltaTime);
        world.update(deltaTime);
    }

    /** Called by the scene stack. */
    public final void render(float deltaTime) {
        if (context.renderer() != null) {
            world.render(context.renderer().renderContext());
        }
        onRender(deltaTime);
    }

    public final void resize(int width, int height) {
        onResize(width, height);
    }

    public final void pause() {
        onPause();
    }

    public final void resume() {
        onResume();
    }

    /** Called by the scene stack: exits then releases everything the scene owns. */
    public final void exit() {
        if (!entered || disposed) {
            return;
        }
        try {
            onExit();
        } finally {
            release();
        }
    }

    private void release() {
        disposed = true;
        try {
            onDispose();
        } finally {
            world.dispose();
            subscriptions.cancelAll();
            InputContext inputContext = inputContext();
            if (inputContext != null) {
                context.input().contexts().remove(inputContext);
            }
            AssetGroup group = assets();
            if (group != null && assetsAcquired) {
                context.assets().unload(group);
                assetsAcquired = false;
            }
        }
    }
}