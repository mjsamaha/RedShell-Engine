package com.lobsterchops.sandbox;

import com.lobsterchops.redshell.audio.AudioCategory;
import com.lobsterchops.redshell.audio.catalog.AudioCatalog;

/** Logical sound IDs. */
public final class SandboxSounds {

    public static final String COIN = "coin";
    public static final String CLICK = "click";
    public static final String HIT = "hit";
    public static final String THEME = "theme";

    private SandboxSounds() {
    }

    public static AudioCatalog catalog() {
        return new AudioCatalog()
                .sound(COIN, SandboxAssets.SFX_COIN, AudioCategory.SFX, 0.8f)
                .sound(HIT, SandboxAssets.SFX_HIT, AudioCategory.SFX, 0.8f)
                .sound(CLICK, SandboxAssets.SFX_CLICK, AudioCategory.UI, 0.7f)
                .music(THEME, SandboxAssets.MUSIC, 0.5f);
    }
}