package com.lobsterchops.redshell.save;

import java.util.List;

/** Tiny string storage used by settings and saves. Backed by LibGDX preferences (browser storage on the web). */
public interface KeyValueStore {

    /** The stored value, or null. */
    String get(String key);

    void put(String key, String value);

    void remove(String key);

    List<String> keys();

    /** Persists pending writes. */
    void flush();
}