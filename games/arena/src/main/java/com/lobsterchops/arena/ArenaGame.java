package com.lobsterchops.arena;

import com.lobsterchops.redshell.app.GameDefinition;
import com.lobsterchops.redshell.app.LaunchConfig;
import com.lobsterchops.redshell.assets.catalog.AssetCatalog;
import com.lobsterchops.redshell.audio.catalog.AudioCatalog;
import com.lobsterchops.redshell.core.config.EngineConfig;
import com.lobsterchops.redshell.debug.log.LogLevel;
import com.lobsterchops.redshell.input.binding.BindingLoader;
import com.lobsterchops.redshell.input.binding.BindingSet;
import com.lobsterchops.redshell.rendering.RenderConfig;
import com.lobsterchops.redshell.rendering.viewport.ViewportPolicy;
import com.lobsterchops.redshell.scene.Scene;

/** A top-down arena shooter: survive waves of chasers, shooting them with the arrow keys. */
public final class ArenaGame implements GameDefinition {

    private static final String BINDINGS =
            "MOVE_LEFT=KEY:A\n"
            + "MOVE_RIGHT=KEY:D\n"
            + "MOVE_UP=KEY:W\n"
            + "MOVE_DOWN=KEY:S\n"
            + "FIRE_LEFT=KEY:LEFT\n"
            + "FIRE_RIGHT=KEY:RIGHT\n"
            + "FIRE_UP=KEY:UP\n"
            + "FIRE_DOWN=KEY:DOWN\n"
            + "CONFIRM=KEY:ENTER,KEY:SPACE\n";

    private final ArenaAssets assets = new ArenaAssets();

    @Override
    public String name() {
        return "RedShell Arena";
    }

    @Override
    public LaunchConfig launchConfig() {
        return LaunchConfig.builder().title("RedShell Arena").windowSize(1280, 720).build();
    }

    @Override
    public EngineConfig engineConfig() {
        return EngineConfig.builder().logLevel(LogLevel.INFO).debug(true).build();
    }

    @Override
    public String logFileName() {
        return "redshell-arena.log";
    }

    @Override
    public RenderConfig renderConfig() {
        return new RenderConfig(640f, 360f, ViewportPolicy.FIT).withClearColor(0.06f, 0.07f, 0.09f, 1f);
    }

    @Override
    public AssetCatalog assetCatalog() {
        return assets.catalog();
    }

    @Override
    public AudioCatalog audioCatalog() {
        return ArenaSounds.catalog();
    }

    @Override
    public String uiFontAssetId() {
        return ArenaAssets.FONT;
    }

    @Override
    public BindingSet defaultBindings() {
        return BindingLoader.parse(BINDINGS, ArenaAction.values());
    }

    @Override
    public Scene createInitialScene() {
        return new MenuScene(assets);
    }
}