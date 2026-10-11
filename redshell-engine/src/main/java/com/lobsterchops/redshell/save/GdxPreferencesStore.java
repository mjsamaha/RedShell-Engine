package com.lobsterchops.redshell.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import java.util.ArrayList;
import java.util.List;

/** {@link KeyValueStore} over LibGDX {@link Preferences}: a file on desktop, local storage in the browser. */
public final class GdxPreferencesStore implements KeyValueStore {

    private final Preferences preferences;

    public GdxPreferencesStore(String name) {
        this.preferences = Gdx.app.getPreferences(name);
    }

    @Override
    public String get(String key) {
        return preferences.contains(key) ? preferences.getString(key) : null;
    }

    @Override
    public void put(String key, String value) {
        preferences.putString(key, value);
    }

    @Override
    public void remove(String key) {
        preferences.remove(key);
    }

    @Override
    public List<String> keys() {
        return new ArrayList<>(preferences.get().keySet());
    }

    @Override
    public void flush() {
        preferences.flush();
    }
}