package com.lobsterchops.redshell.core.config;

import java.util.HashMap;
import java.util.Map;

/** Central lookup for named configuration objects (engine, launch, rendering, ...). */
public final class ConfigRegistry {

    private final Map<String, Object> configs = new HashMap<>();

    public void register(String name, Object config) {
        if (name == null || config == null) {
            throw new IllegalArgumentException("name and config must not be null");
        }
        if (configs.containsKey(name)) {
            throw new IllegalStateException("Config already registered: " + name);
        }
        configs.put(name, config);
    }

    public boolean has(String name) {
        return configs.containsKey(name);
    }

    // Class.cast/isInstance are not available under GWT, so a wrong type fails with ClassCastException at the call site.
    @SuppressWarnings("unchecked")
    public <T> T get(String name, Class<T> type) {
        Object config = configs.get(name);
        if (config == null) {
            throw new IllegalStateException("No config registered under: " + name);
        }
        return (T) config;
    }
}
