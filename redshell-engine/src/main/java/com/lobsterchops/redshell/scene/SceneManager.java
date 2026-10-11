package com.lobsterchops.redshell.scene;

import com.lobsterchops.redshell.assets.AssetService;
import com.lobsterchops.redshell.core.context.EngineContext;
import com.lobsterchops.redshell.core.lifecycle.EngineSystem;
import com.lobsterchops.redshell.debug.log.Log;
import com.lobsterchops.redshell.entity.prefab.PrefabRegistry;
import com.lobsterchops.redshell.events.EventBus;
import com.lobsterchops.redshell.events.types.PausedEvent;
import com.lobsterchops.redshell.events.types.SceneChangedEvent;
import com.lobsterchops.redshell.events.types.WindowResizedEvent;
import com.lobsterchops.redshell.input.InputService;
import com.lobsterchops.redshell.rendering.Renderer2D;
import com.lobsterchops.redshell.scene.loading.LoadingScene;
import com.lobsterchops.redshell.scene.stack.SceneStack;
import com.lobsterchops.redshell.scene.transition.InstantTransition;
import com.lobsterchops.redshell.scene.transition.Transition;
import java.util.ArrayList;
import java.util.List;

/**
 * Owns the scene stack and drives it as an engine system. Scene changes requested through
 * {@link #push}, {@link #pop}, {@link #replace}, {@link #replaceAll} and {@link #load} are queued and applied
 * between frames (at the midpoint of the transition), so a scene can safely ask for a change from inside its
 * own update.
 */
public final class SceneManager implements EngineSystem {

    private static final String TAG = "Scenes";

    private enum Kind {
        PUSH,
        POP,
        REPLACE,
        REPLACE_ALL
    }

    private static final class Request {
        private final Kind kind;
        private final Scene scene;
        private final Transition transition;

        Request(Kind kind, Scene scene, Transition transition) {
            this.kind = kind;
            this.scene = scene;
            this.transition = transition;
        }
    }

    private final Scene initialScene;
    private final List<Request> queue = new ArrayList<>();
    private SceneStack stack;
    private SceneContext sceneContext;
    private EventBus events;
    private Log log;
    private Renderer2D renderer;
    private AssetService assets;
    private Request pending;
    private Transition transition;

    /** The scene to start with. It is loaded through a {@link LoadingScene} if it declares assets. */
    public SceneManager(Scene initialScene) {
        this.initialScene = initialScene;
    }

    @Override
    public void initialize(EngineContext context) {
        log = context.log();
        events = context.service(EventBus.class);
        assets = context.service(AssetService.class);
        renderer = context.hasService(Renderer2D.class) ? context.service(Renderer2D.class) : null;
        sceneContext = new SceneContext(context, assets, context.service(InputService.class), events,
                context.service(PrefabRegistry.class), renderer, this, log);
        stack = new SceneStack(sceneContext);
        load(initialScene, new InstantTransition());
        processQueue();
    }

    public void push(Scene scene) {
        push(scene, new InstantTransition());
    }

    public void push(Scene scene, Transition with) {
        queue.add(new Request(Kind.PUSH, scene, with));
    }

    public void pop() {
        pop(new InstantTransition());
    }

    public void pop(Transition with) {
        queue.add(new Request(Kind.POP, null, with));
    }

    public void replace(Scene scene) {
        replace(scene, new InstantTransition());
    }

    public void replace(Scene scene, Transition with) {
        queue.add(new Request(Kind.REPLACE, scene, with));
    }

    /** Exits every scene and starts the given one. */
    public void replaceAll(Scene scene, Transition with) {
        queue.add(new Request(Kind.REPLACE_ALL, scene, with));
    }

    public void load(Scene target) {
        load(target, new InstantTransition());
    }

    /**
     * Replaces everything with the target. If the target declares assets, a {@link LoadingScene} shows progress
     * while they load asynchronously, then the target is started with a copy of the transition.
     */
    public void load(Scene target, Transition with) {
        if (target.assets() == null) {
            replaceAll(target, with);
        } else {
            replaceAll(new LoadingScene(target, with.copy()), with);
        }
    }

    public Scene current() {
        return stack.top();
    }

    public SceneStack stack() {
        return stack;
    }

    public boolean isTransitioning() {
        return transition != null || !queue.isEmpty();
    }

    @Override
    public void update(float deltaTime) {
        processQueue();
        if (transition != null) {
            transition.update(deltaTime);
            advanceTransition();
        }
        stack.update(deltaTime);
    }

    @Override
    public void render(float deltaTime) {
        stack.render(deltaTime);
        if (transition != null && renderer != null) {
            transition.render(renderer);
        }
    }

    @Override
    public void resize(int width, int height) {
        stack.resize(width, height);
        events.publish(new WindowResizedEvent(width, height));
    }

    @Override
    public void pause() {
        stack.pause();
        events.publish(new PausedEvent(true));
    }

    @Override
    public void resume() {
        stack.resume();
        events.publish(new PausedEvent(false));
    }

    @Override
    public void dispose() {
        queue.clear();
        pending = null;
        transition = null;
        if (stack != null) {
            stack.clear();
        }
    }

    private void processQueue() {
        while (transition == null && !queue.isEmpty()) {
            Request request = queue.remove(0);
            transition = request.transition;
            pending = request;
            advanceTransition();
        }
    }

    private void advanceTransition() {
        if (pending != null && transition.reachedMidpoint()) {
            Request request = pending;
            pending = null;
            apply(request);
        }
        if (pending == null && transition.isFinished()) {
            transition = null;
        }
    }

    private void apply(Request request) {
        Scene before = stack.top();
        switch (request.kind) {
            case PUSH:
                stack.push(request.scene);
                break;
            case POP:
                if (stack.size() == 0) {
                    log.warn(TAG, "Pop ignored: the stack is empty");
                } else {
                    stack.pop();
                }
                break;
            case REPLACE:
                stack.replace(request.scene);
                break;
            default:
                stack.clear();
                stack.push(request.scene);
                break;
        }
        Scene after = stack.top();
        if (before != after) {
            log.debug(TAG, (before == null ? "none" : before.name()) + " -> " + (after == null ? "none" : after.name()));
            events.publish(new SceneChangedEvent(before == null ? null : before.name(),
                    after == null ? null : after.name()));
        }
    }
}