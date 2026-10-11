package com.lobsterchops.redshell.app;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.lobsterchops.redshell.assets.catalog.AssetCatalog;
import com.lobsterchops.redshell.assets.validation.AssetValidator;
import com.lobsterchops.redshell.assets.validation.GdxFileChecker;
import com.lobsterchops.redshell.core.lifecycle.ScopeListener;
import com.lobsterchops.redshell.debug.log.MemoryLogSink;
import com.lobsterchops.redshell.debug.filelog.FileLogSink;
import com.lobsterchops.redshell.debug.overlay.DebugOverlay;
import com.lobsterchops.redshell.debug.profiler.Profiler;
import java.util.List;
import com.badlogic.gdx.Gdx;
import com.lobsterchops.redshell.core.Engine;
import com.lobsterchops.redshell.core.EngineBuilder;
import com.lobsterchops.redshell.core.config.EngineConfig;
import com.lobsterchops.redshell.debug.log.ConsoleLogSink;
import com.lobsterchops.redshell.debug.log.ExceptionReporter;
import com.lobsterchops.redshell.debug.log.Log;
import com.lobsterchops.redshell.rendering.RenderConfig;
import com.lobsterchops.redshell.rendering.Renderer2D;
import com.lobsterchops.redshell.assets.AssetService;
import com.lobsterchops.redshell.assets.GdxAssetService;
import com.lobsterchops.redshell.entity.prefab.PrefabRegistry;
import com.lobsterchops.redshell.events.EventBus;
import com.lobsterchops.redshell.input.InputRouter;
import com.lobsterchops.redshell.input.InputService;
import com.lobsterchops.redshell.input.device.InputDevice;
import com.lobsterchops.redshell.input.device.KeyboardDevice;
import com.lobsterchops.redshell.input.device.MouseDevice;
import com.lobsterchops.redshell.scene.SceneManager;
import com.lobsterchops.redshell.audio.AudioService;
import com.lobsterchops.redshell.audio.GdxAudioService;
import com.lobsterchops.redshell.input.action.Action;
import com.lobsterchops.redshell.input.binding.BindingSet;
import com.lobsterchops.redshell.save.GdxPreferencesStore;
import com.lobsterchops.redshell.save.KeyValueStore;
import com.lobsterchops.redshell.save.MemoryKeyValueStore;
import com.lobsterchops.redshell.save.data.SaveService;
import com.lobsterchops.redshell.save.settings.SettingsService;
import com.lobsterchops.redshell.ui.UiService;

/**
 * The only bridge between LibGDX's lifecycle and the engine. It builds the engine in {@code create}
 * and forwards every callback; there is no gameplay logic and no second loop here.
 * This is the composition root, so it may reference any engine package.
 *
 * <p>When no OpenGL context exists (headless backends) no renderer is created and nothing is drawn.
 */
public final class RedShellApplication implements ApplicationListener {

    private static final String TAG = "App";

    private final GameDefinition definition;
    private Log log;
    private Engine engine;
    private MemoryLogSink memoryLogs;

    public RedShellApplication(GameDefinition definition) {
        this.definition = definition;
    }

    @Override
    public void create() {
        EngineConfig engineConfig = definition.engineConfig();
        log = new Log(engineConfig.logLevel(), new ConsoleLogSink());
        if (engineConfig.debugEnabled()) {
            memoryLogs = new MemoryLogSink(200);
            log.addSink(memoryLogs);
            if (definition.logFileName() != null && Gdx.app != null
                    && Gdx.app.getType() == Application.ApplicationType.Desktop) {
                log.addSink(new FileLogSink(Gdx.files.external(".redshell/" + definition.logFileName())));
            }
        }
        log.info(TAG, "Starting " + definition.name());
        try {
            engine = assemble(engineConfig);
            engine.start();
        } catch (RuntimeException | Error e) {
            new ExceptionReporter(log).report("RedShellApplication.create", e);
            throw e;
        }
    }

    private Engine assemble(EngineConfig engineConfig) {
        EventBus events = new EventBus(log);
        PrefabRegistry prefabs = new PrefabRegistry();
        EngineBuilder builder = new EngineBuilder(engineConfig, log)
                .config(LaunchConfig.REGISTRY_NAME, definition.launchConfig())
                .service(EventBus.class, events)
                .service(PrefabRegistry.class, prefabs);
        if (Gdx.gl != null) {
            RenderConfig renderConfig = definition.renderConfig();
            Renderer2D renderer = new Renderer2D(renderConfig);
            builder.config(RenderConfig.REGISTRY_NAME, renderConfig)
                    .service(Renderer2D.class, renderer)
                    .renderBracket(renderer)
                    .system(renderer);
        } else {
            log.warn(TAG, "No OpenGL context available; rendering is disabled");
        }
        GdxAssetService assets = new GdxAssetService(definition.assetCatalog());
        InputDevice[] devices = Gdx.input == null
                ? new InputDevice[0]
                : new InputDevice[] {new KeyboardDevice(), new MouseDevice()};
        BindingSet defaults = definition.defaultBindings();
        InputService input = new InputService(defaults, devices);
        InputRouter router = new InputRouter();
        KeyValueStore store = Gdx.app != null
                ? new GdxPreferencesStore(definition.storageName())
                : new MemoryKeyValueStore();
        SettingsService settings = new SettingsService(store, defaults.actions().toArray(new Action[0]));
        settings.applyBindings(input);
        settings.applyDisplay(true);
        definition.registerPrefabs(prefabs, assets);
        builder.service(AssetService.class, assets)
                .service(InputService.class, input)
                .service(InputRouter.class, router)
                .service(KeyValueStore.class, store)
                .service(SettingsService.class, settings)
                .service(SaveService.class, new SaveService(store, definition.saveVersion(), definition.saveMigrations()))
                .system(assets);
        if (Gdx.audio != null) {
            GdxAudioService audio = new GdxAudioService(definition.audioCatalog(), assets);
            settings.applyTo(audio.mixer());
            builder.service(AudioService.class, audio).system(audio);
        } else {
            log.warn(TAG, "No audio device available; audio is disabled");
        }
        validateAssets(definition.assetCatalog());
        SceneManager sceneManager = new SceneManager(definition.createInitialScene());
        builder.service(SceneManager.class, sceneManager);
        builder.system(input).system(router).system(sceneManager);
        if (Gdx.gl != null && definition.uiFontAssetId() != null) {
            UiService ui = new UiService(definition.uiFontAssetId());
            builder.service(UiService.class, ui).system(ui);
        }
        if (engineConfig.debugEnabled()) {
            Profiler profiler = new Profiler(120);
            builder.service(ScopeListener.class, profiler).service(Profiler.class, profiler).scopeListener(profiler);
            if (Gdx.gl != null && Gdx.input != null) {
                builder.system(new DebugOverlay(definition.uiFontAssetId(), profiler, memoryLogs));
            }
        }
        return builder.build();
    }

    private void validateAssets(AssetCatalog catalog) {
        if (Gdx.files == null) {
            return;
        }
        List<String> problems = new AssetValidator(new GdxFileChecker()).validate(catalog);
        for (int i = 0; i < problems.size(); i++) {
            log.error(TAG, "Asset validation: " + problems.get(i));
        }
        if (problems.isEmpty()) {
            log.debug(TAG, "Asset validation passed (" + catalog.entries().size() + " assets)");
        }
    }
    @Override
    public void render() {
        engine.frame(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void resize(int width, int height) {
        engine.resize(width, height);
    }

    @Override
    public void pause() {
        engine.pause();
    }

    @Override
    public void resume() {
        engine.resume();
    }

    @Override
    public void dispose() {
        if (engine != null) {
            engine.shutdown();
        }
    }
}
