package com.lobsterchops.redshell.entity.prefab;

import java.util.HashMap;
import java.util.Map;

/** Looks prefabs up by ID. Spawn them with {@code World.spawn} or an {@code EntityCommandBuffer}. */
public final class PrefabRegistry {

    private final Map<String, Prefab> prefabs = new HashMap<>();

    public PrefabRegistry register(String id, Prefab prefab) {
        if (prefabs.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate prefab id: " + id);
        }
        prefabs.put(id, prefab);
        return this;
    }

    public boolean has(String id) {
        return prefabs.containsKey(id);
    }

    public Prefab get(String id) {
        Prefab prefab = prefabs.get(id);
        if (prefab == null) {
            throw new IllegalArgumentException("Unknown prefab id: " + id);
        }
        return prefab;
    }
}