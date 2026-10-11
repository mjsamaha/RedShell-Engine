package com.lobsterchops.arena;

import com.lobsterchops.redshell.assets.catalog.AssetCatalog;
import com.lobsterchops.redshell.assets.catalog.AssetGroup;
import com.lobsterchops.redshell.assets.catalog.AssetType;

/** Logical asset IDs and groups for the arena game. */
public final class ArenaAssets {

    public static final String FONT = "ui.font";
    public static final String FLOOR = "world.floor";
    public static final String PLAYER = "actor.player";
    public static final String ENEMY = "actor.enemy";
    public static final String SFX_KILL = "sfx.kill";
    public static final String SFX_HIT = "sfx.hit";
    public static final String SFX_CLICK = "sfx.click";
    public static final String MUSIC = "music.theme";

    private final AssetCatalog catalog = new AssetCatalog();
    private final AssetGroup menu;
    private final AssetGroup gameplay;

    public ArenaAssets() {
        catalog.register(FONT, "arena/font.fnt", AssetType.BITMAP_FONT)
                .register(FLOOR, "arena/tile.png", AssetType.TEXTURE)
                .register(PLAYER, "arena/player.png", AssetType.TEXTURE)
                .register(ENEMY, "arena/enemy.png", AssetType.TEXTURE)
                .register(SFX_KILL, "arena/coin.wav", AssetType.SOUND)
                .register(SFX_HIT, "arena/hit.wav", AssetType.SOUND)
                .register(SFX_CLICK, "arena/click.wav", AssetType.SOUND)
                .register(MUSIC, "arena/music.wav", AssetType.MUSIC);
        menu = catalog.defineGroup("menu", FONT, SFX_CLICK);
        gameplay = catalog.defineGroup("gameplay", FONT, SFX_CLICK, SFX_KILL, SFX_HIT, FLOOR, PLAYER, ENEMY);
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