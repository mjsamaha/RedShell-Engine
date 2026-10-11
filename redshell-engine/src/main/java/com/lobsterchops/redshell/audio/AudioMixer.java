package com.lobsterchops.redshell.audio;

/** Volume maths: master, per-category and mute. Pure logic, no LibGDX. */
public final class AudioMixer {

    private final float[] categoryVolumes = new float[AudioCategory.values().length];
    private float masterVolume = 1f;
    private boolean muted;

    public AudioMixer() {
        for (int i = 0; i < categoryVolumes.length; i++) {
            categoryVolumes[i] = 1f;
        }
    }

    public float masterVolume() {
        return masterVolume;
    }

    public void setMasterVolume(float volume) {
        this.masterVolume = clamp(volume);
    }

    public float categoryVolume(AudioCategory category) {
        return categoryVolumes[category.ordinal()];
    }

    public void setCategoryVolume(AudioCategory category, float volume) {
        categoryVolumes[category.ordinal()] = clamp(volume);
    }

    public boolean muted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    /** The volume to hand to the backend for a sound with the given base volume. Zero while muted. */
    public float effectiveVolume(AudioCategory category, float baseVolume) {
        if (muted) {
            return 0f;
        }
        return clamp(baseVolume) * masterVolume * categoryVolumes[category.ordinal()];
    }

    private static float clamp(float value) {
        return value < 0f ? 0f : (value > 1f ? 1f : value);
    }
}