package com.lobsterchops.sandbox;

import com.lobsterchops.redshell.assets.catalog.AssetCatalog;
import com.lobsterchops.redshell.assets.catalog.AssetGroup;
import com.lobsterchops.redshell.assets.catalog.AssetType;

/** Logical asset IDs and groups for the sandbox. Scenes never see file paths. */
public final class SandboxAssets {

    public static final String FONT = "ui.font";
    public static final String TILE = "world.tile";
    public static final String WALL = "world.wall";
    public static final String COIN = "world.coin";
    public static final String ENEMY = "actor.enemy";
    public static final String PLAYER = "actor.player";
    public static final String SFX_COIN = "sfx.coin";
    public static final String SFX_CLICK = "sfx.click";
    public static final String SFX_HIT = "sfx.hit";
    public static final String MUSIC = "music.theme";

    private final AssetCatalog catalog = new AssetCatalog();
    private final AssetGroup menu;
    private final AssetGroup gameplay;

    public SandboxAssets() {
        catalog.register(FONT, "sandbox/font.fnt", AssetType.BITMAP_FONT)
                .register(TILE, "sandbox/tile.png", AssetType.TEXTURE)
                .register(WALL, "sandbox/wall.png", AssetType.TEXTURE)
                .register(COIN, "sandbox/coin.png", AssetType.TEXTURE)
                .register(ENEMY, "sandbox/enemy.png", AssetType.TEXTURE)
                .register(PLAYER, "sandbox/player.png", AssetType.TEXTURE)
                .register(SFX_COIN, "sandbox/coin.wav", AssetType.SOUND)
                .register(SFX_CLICK, "sandbox/click.wav", AssetType.SOUND)
                .register(SFX_HIT, "sandbox/hit.wav", AssetType.SOUND)
                .register(MUSIC, "sandbox/music.wav", AssetType.MUSIC);
        menu = catalog.defineGroup("menu", FONT, SFX_CLICK);
        gameplay = catalog.defineGroup("gameplay", FONT, SFX_CLICK, SFX_COIN, SFX_HIT, TILE, WALL, COIN, ENEMY, PLAYER);
    }

    public AssetCatalog catalog() {
        return catalog;
    }

    public AssetGroup menu() {
        return menu;
    }

    public AssetGroup gameplay() {
        return gameplay;
    }
}