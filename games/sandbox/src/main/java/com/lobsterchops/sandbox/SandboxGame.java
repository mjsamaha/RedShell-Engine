package com.lobsterchops.sandbox;

import com.lobsterchops.redshell.app.GameDefinition;
import com.lobsterchops.redshell.app.LaunchConfig;
import com.lobsterchops.redshell.assets.AssetService;
import com.lobsterchops.redshell.assets.catalog.AssetCatalog;
import com.lobsterchops.redshell.audio.catalog.AudioCatalog;
import com.lobsterchops.redshell.core.config.EngineConfig;
import com.lobsterchops.redshell.debug.log.LogLevel;
import com.lobsterchops.redshell.entity.prefab.PrefabRegistry;
import com.lobsterchops.redshell.input.binding.BindingLoader;
import com.lobsterchops.redshell.input.binding.BindingSet;
import com.lobsterchops.redshell.rendering.RenderConfig;
import com.lobsterchops.redshell.rendering.viewport.ViewportPolicy;
import com.lobsterchops.redshell.scene.Scene;

/** The sandbox game: a menu, a small tile map with an animated player, and a pause overlay. */
public final class SandboxGame implements GameDefinition {

    private static final String BINDINGS =
            "MOVE_LEFT=KEY:A,KEY:LEFT\n"
            + "MOVE_RIGHT=KEY:D,KEY:RIGHT\n"
            + "MOVE_UP=KEY:W,KEY:UP\n"
            + "MOVE_DOWN=KEY:S,KEY:DOWN\n"
            + "CONFIRM=KEY:ENTER,KEY:SPACE\n"
            + "PAUSE=KEY:ESCAPE,KEY:P\n"
            + "BACK=KEY:Q\n";

    private final SandboxAssets assets = new SandboxAssets();

    @Override
    public String name() {
        return "RedShell Sandbox";
    }

    @Override
    public LaunchConfig launchConfig() {
        return LaunchConfig.builder()
                .title("RedShell Sandbox")
                .windowSize(1280, 720)
                .build();
    }

    @Override
    public EngineConfig engineConfig() {
        return EngineConfig.builder().logLevel(LogLevel.DEBUG).build();
    }

    @Override
    public RenderConfig renderConfig() {
        return new RenderConfig(640f, 360f, ViewportPolicy.FIT).withClearColor(0.08f, 0.08f, 0.1f, 1f);
    }

    @Override
    public AssetCatalog assetCatalog() {
        return assets.catalog();
    }

    @Override
    public AudioCatalog audioCatalog() {
        return SandboxSounds.catalog();
    }

    @Override
    public String uiFontAssetId() {
        return SandboxAssets.FONT;
    }

    @Override
    public BindingSet defaultBindings() {
        return BindingLoader.parse(BINDINGS, SandboxAction.values());
    }

    @Override
    public void registerPrefabs(PrefabRegistry prefabs, AssetService assetService) {
        prefabs.register("player", new PlayerPrefab(assetService));
    }

    @Override
    public Scene createInitialScene() {
        return new MenuScene(assets);
    }
}