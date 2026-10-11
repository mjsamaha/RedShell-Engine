package com.lobsterchops.redshell.save.settings;

import com.lobsterchops.redshell.audio.AudioCategory;

/** Player-facing options that persist between sessions. */
public final class Settings {

    public float masterVolume = 1f;
    public final float[] categoryVolumes = new float[AudioCategory.values().length];
    public boolean muted;
    public boolean fullscreen;
    public boolean vsync = true;
    /** Key bindings in {@code BindingSet.toText()} form, or null to keep the defaults. */
    public String bindings;

    public Settings() {
        for (int i = 0; i < categoryVolumes.length; i++) {
            categoryVolumes[i] = 1f;
        }
    }
}