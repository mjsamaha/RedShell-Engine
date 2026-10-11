package com.lobsterchops.redshell.examples;

import com.lobsterchops.redshell.app.GameDefinition;
import com.lobsterchops.redshell.app.LaunchConfig;
import com.lobsterchops.redshell.assets.catalog.AssetCatalog;
import com.lobsterchops.redshell.assets.catalog.AssetGroup;
import com.lobsterchops.redshell.assets.catalog.AssetType;
import com.lobsterchops.redshell.audio.AudioCategory;
import com.lobsterchops.redshell.audio.catalog.AudioCatalog;
import com.lobsterchops.redshell.core.config.EngineConfig;
import com.lobsterchops.redshell.input.binding.BindingLoader;
import com.lobsterchops.redshell.input.binding.BindingSet;
import com.lobsterchops.redshell.rendering.RenderConfig;
import com.lobsterchops.redshell.rendering.viewport.ViewportPolicy;

/**
 * Shared setup for the examples: a 640x360 world, the debug overlay (F3), a small asset catalog that reuses the
 * sandbox's art and sounds, and common WASD/arrow bindings. Each example only adds its own scene.
 */
abstract class ExampleGame implements GameDefinition {

    static final String FONT = "font";
    static final String TILE = "tile";
    static final String COIN = "coin";
    static final String PLAYER = "player";
    static final String SFX_COIN = "sfx.coin";
    static final String SFX_CLICK = "sfx.click";
    static final String MUSIC = "music";
    static final String SOUND_COIN = "coin";
    static final String SOUND_CLICK = "click";
    static final String SONG = "theme";

    private static final String BINDINGS =
            "LEFT=KEY:A,KEY:LEFT\n"
            + "RIGHT=KEY:D,KEY:RIGHT\n"
            + "UP=KEY:W,KEY:UP\n"
            + "DOWN=KEY:S,KEY:DOWN\n"
            + "PRIMARY=KEY:SPACE\n"
            + "SECONDARY=KEY:E\n"
            + "BACK=KEY:Q\n";

    private final AssetCatalog catalog = new AssetCatalog();
    private final AssetGroup group;

    ExampleGame() {
        catalog.register(FONT, "sandbox/font.fnt", AssetType.BITMAP_FONT)
                .register(TILE, "sandbox/tile.png", AssetType.TEXTURE)
                .register(COIN, "sandbox/coin.png", AssetType.TEXTURE)
                .register(PLAYER, "sandbox/player.png", AssetType.TEXTURE)
                .register(SFX_COIN, "sandbox/coin.wav", AssetType.SOUND)
                .register(SFX_CLICK, "sandbox/click.wav", AssetType.SOUND)
                .register(MUSIC, "sandbox/music.wav", AssetType.MUSIC);
        group = catalog.defineGroup("example", FONT, TILE, COIN, PLAYER, SFX_COIN, SFX_CLICK);
    }

    /** The one asset group every example scene declares. */
    final AssetGroup group() {
        return group;
    }

    @Override
    public LaunchConfig launchConfig() {
        return LaunchConfig.builder().title(name()).windowSize(1280, 720).build();
    }

    @Override
    public EngineConfig engineConfig() {
        return EngineConfig.builder().debug(true).build();
    }

    @Override
    public RenderConfig renderConfig() {
        return new RenderConfig(640f, 360f, ViewportPolicy.FIT).withClearColor(0.08f, 0.08f, 0.1f, 1f);
    }

    @Override
    public AssetCatalog assetCatalog() {
        return catalog;
    }

    @Override
    public AudioCatalog audioCatalog() {
        return new AudioCatalog()
                .sound(SOUND_COIN, SFX_COIN, AudioCategory.SFX, 0.8f)
                .sound(SOUND_CLICK, SFX_CLICK, AudioCategory.UI, 0.7f)
                .music(SONG, MUSIC, 0.5f);
    }

    @Override
    public String uiFontAssetId() {
        return FONT;
    }

    @Override
    public BindingSet defaultBindings() {
        return BindingLoader.parse(BINDINGS, ExampleAction.values());
    }
}