package com.lobsterchops.redshell.scene;

import com.lobsterchops.redshell.assets.AssetService;
import com.lobsterchops.redshell.core.context.EngineContext;
import com.lobsterchops.redshell.debug.log.Log;
import com.lobsterchops.redshell.entity.prefab.PrefabRegistry;
import com.lobsterchops.redshell.events.EventBus;
import com.lobsterchops.redshell.input.InputService;
import com.lobsterchops.redshell.rendering.Renderer2D;

/** Everything a scene may use: engine services, handed in when the scene enters. */
public final class SceneContext {

    private final EngineContext engine;
    private final AssetService assets;
    private final InputService input;
    private final EventBus events;
    private final PrefabRegistry prefabs;
    private final Renderer2D renderer;
    private final SceneManager scenes;
    private final Log log;

    public SceneContext(EngineContext engine, AssetService assets, InputService input, EventBus events,
                        PrefabRegistry prefabs, Renderer2D renderer, SceneManager scenes, Log log) {
        this.engine = engine;
        this.assets = assets;
        this.input = input;
        this.events = events;
        this.prefabs = prefabs;
        this.renderer = renderer;
        this.scenes = scenes;
        this.log = log;
    }

    public EngineContext engine() {
        return engine;
    }

    public AssetService assets() {
        return assets;
    }

    public InputService input() {
        return input;
    }

    public EventBus events() {
        return events;
    }

    public PrefabRegistry prefabs() {
        return prefabs;
    }

    /** The renderer; null when running without an OpenGL context. */
    public Renderer2D renderer() {
        return renderer;
    }

    public SceneManager scenes() {
        return scenes;
    }

    public Log log() {
        return log;
    }
}