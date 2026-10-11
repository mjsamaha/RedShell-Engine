package com.lobsterchops.redshell.save;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Non-persistent store, for tests and headless runs. */
public final class MemoryKeyValueStore implements KeyValueStore {

    private final Map<String, String> values = new LinkedHashMap<>();

    @Override
    public String get(String key) {
        return values.get(key);
    }

    @Override
    public void put(String key, String value) {
        values.put(key, value);
    }

    @Override
    public void remove(String key) {
        values.remove(key);
    }

    @Override
    public List<String> keys() {
        return new ArrayList<>(values.keySet());
    }

    @Override
    public void flush() {
    }
}