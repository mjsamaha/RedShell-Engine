package com.lobsterchops.arena;

import com.lobsterchops.redshell.audio.AudioCategory;
import com.lobsterchops.redshell.audio.catalog.AudioCatalog;

/** Logical sound IDs. */
public final class ArenaSounds {

    public static final String KILL = "kill";
    public static final String HIT = "hit";
    public static final String CLICK = "click";
    public static final String THEME = "theme";

    private ArenaSounds() {
    }

    public static AudioCatalog catalog() {
        return new AudioCatalog()
                .sound(KILL, ArenaAssets.SFX_KILL, AudioCategory.SFX, 0.6f)
                .sound(HIT, ArenaAssets.SFX_HIT, AudioCategory.SFX, 0.9f)
                .sound(CLICK, ArenaAssets.SFX_CLICK, AudioCategory.UI, 0.7f)
                .music(THEME, ArenaAssets.MUSIC, 0.4f);
    }
}