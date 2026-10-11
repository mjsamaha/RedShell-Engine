package com.lobsterchops.redshell.audio.catalog;

import com.lobsterchops.redshell.audio.AudioCategory;

/** One playable sound or music track: the asset behind it, its category and its base volume. */
public final class SoundDefinition {

    private final String id;
    private final String assetId;
    private final AudioCategory category;
    private final float volume;
    private final boolean music;

    public SoundDefinition(String id, String assetId, AudioCategory category, float volume, boolean music) {
        this.id = id;
        this.assetId = assetId;
        this.category = category;
        this.volume = volume;
        this.music = music;
    }

    public String id() {
        return id;
    }

    /** Logical ID in the {@code AssetCatalog}. */
    public String assetId() {
        return assetId;
    }

    public AudioCategory category() {
        return category;
    }

    public float volume() {
        return volume;
    }

    public boolean isMusic() {
        return music;
    }
}