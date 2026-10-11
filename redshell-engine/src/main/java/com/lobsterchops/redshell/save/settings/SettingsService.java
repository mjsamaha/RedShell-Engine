package com.lobsterchops.redshell.save.settings;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.lobsterchops.redshell.audio.AudioCategory;
import com.lobsterchops.redshell.audio.AudioMixer;
import com.lobsterchops.redshell.input.InputService;
import com.lobsterchops.redshell.input.action.Action;
import com.lobsterchops.redshell.input.binding.BindingLoader;
import com.lobsterchops.redshell.save.KeyValueStore;

/** Loads, holds and persists {@link Settings}, and applies them to audio, input and the display. */
public final class SettingsService {

    private static final String P = "settings.";

    private final KeyValueStore store;
    private final Action[] actions;
    private final Settings settings = new Settings();

    public SettingsService(KeyValueStore store, Action... actions) {
        this.store = store;
        this.actions = actions;
        load();
    }

    public Settings settings() {
        return settings;
    }

    public final void load() {
        settings.masterVolume = readFloat("master", 1f);
        AudioCategory[] categories = AudioCategory.values();
        for (int i = 0; i < categories.length; i++) {
            settings.categoryVolumes[i] = readFloat("volume." + categories[i].name(), 1f);
        }
        settings.muted = "true".equals(store.get(P + "muted"));
        settings.fullscreen = "true".equals(store.get(P + "fullscreen"));
        settings.vsync = !"false".equals(store.get(P + "vsync"));
        settings.bindings = store.get(P + "bindings");
    }

    public void save() {
        store.put(P + "master", Float.toString(settings.masterVolume));
        AudioCategory[] categories = AudioCategory.values();
        for (int i = 0; i < categories.length; i++) {
            store.put(P + "volume." + categories[i].name(), Float.toString(settings.categoryVolumes[i]));
        }
        store.put(P + "muted", Boolean.toString(settings.muted));
        store.put(P + "fullscreen", Boolean.toString(settings.fullscreen));
        store.put(P + "vsync", Boolean.toString(settings.vsync));
        if (settings.bindings != null) {
            store.put(P + "bindings", settings.bindings);
        }
        store.flush();
    }

    /** Copies the stored volumes into the mixer. */
    public void applyTo(AudioMixer mixer) {
        mixer.setMasterVolume(settings.masterVolume);
        AudioCategory[] categories = AudioCategory.values();
        for (int i = 0; i < categories.length; i++) {
            mixer.setCategoryVolume(categories[i], settings.categoryVolumes[i]);
        }
        mixer.setMuted(settings.muted);
    }

    /** Copies the mixer's volumes into the settings (call {@link #save()} afterwards). */
    public void captureFrom(AudioMixer mixer) {
        settings.masterVolume = mixer.masterVolume();
        AudioCategory[] categories = AudioCategory.values();
        for (int i = 0; i < categories.length; i++) {
            settings.categoryVolumes[i] = mixer.categoryVolume(categories[i]);
        }
        settings.muted = mixer.muted();
    }

    /** Replaces the input bindings with the saved ones, if any. A broken saved profile is ignored. */
    public void applyBindings(InputService input) {
        if (settings.bindings == null) {
            return;
        }
        try {
            input.setBindings(BindingLoader.parse(settings.bindings, actions));
        } catch (IllegalArgumentException e) {
            settings.bindings = null;
        }
    }

    /** Stores the input service's current bindings (call {@link #save()} afterwards). */
    public void captureBindings(InputService input) {
        settings.bindings = input.bindings().toText();
    }

    /** Applies fullscreen and vsync. Fullscreen is only forced on desktop at startup. */
    public void applyDisplay(boolean startup) {
        if (Gdx.graphics == null) {
            return;
        }
        Gdx.graphics.setVSync(settings.vsync);
        boolean desktop = Gdx.app != null && Gdx.app.getType() == Application.ApplicationType.Desktop;
        if (settings.fullscreen && (desktop || !startup)) {
            if (!Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }
        } else if (!settings.fullscreen && Gdx.graphics.isFullscreen()) {
            Gdx.graphics.setWindowedMode(1280, 720);
        }
    }

    private float readFloat(String key, float fallback) {
        String value = store.get(P + key);
        if (value == null) {
            return fallback;
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}