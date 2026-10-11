package com.lobsterchops.redshell.app;

import com.lobsterchops.redshell.assets.AssetService;
import com.lobsterchops.redshell.assets.catalog.AssetCatalog;
import com.lobsterchops.redshell.audio.catalog.AudioCatalog;
import com.lobsterchops.redshell.core.config.EngineConfig;
import com.lobsterchops.redshell.entity.prefab.PrefabRegistry;
import com.lobsterchops.redshell.input.binding.BindingSet;
import com.lobsterchops.redshell.rendering.RenderConfig;
import com.lobsterchops.redshell.save.data.Migration;
import com.lobsterchops.redshell.scene.Scene;

/** What a game implements to describe itself. Games never implement LibGDX's {@code ApplicationListener}. */
public interface GameDefinition {

    String name();

    LaunchConfig launchConfig();

    default EngineConfig engineConfig() {
        return EngineConfig.defaults();
    }

    default RenderConfig renderConfig() {
        return RenderConfig.defaults();
    }

    /** Every asset the game uses, by logical ID, with its groups. */
    default AssetCatalog assetCatalog() {
        return new AssetCatalog();
    }

    /** Every sound and music track, by logical ID. Their assets must also be in the asset catalog. */
    default AudioCatalog audioCatalog() {
        return new AudioCatalog();
    }

    /** Catalog ID of the UI bitmap font, or null if the game uses no engine UI. */
    default String uiFontAssetId() {
        return null;
    }

    /** Name of the preferences file / browser storage entry holding settings and saves. */
    default String storageName() {
        return name().toLowerCase().replace(' ', '-');
    }

    /** Log file name on desktop (written to ~/.redshell/), or null for no log file. Browsers keep logs in memory only. */
    default String logFileName() {
        return null;
    }

    /** Current save format version. */
    default int saveVersion() {
        return 1;
    }

    default Migration[] saveMigrations() {
        return new Migration[0];
    }

    /** The default action bindings. */
    default BindingSet defaultBindings() {
        return new BindingSet();
    }

    /** Registers the game's prefabs. Prefabs may capture the asset service and use it when spawned. */
    default void registerPrefabs(PrefabRegistry prefabs, AssetService assets) {
    }

    /** The first scene. If it declares assets it is shown behind a loading screen. */
    Scene createInitialScene();
}