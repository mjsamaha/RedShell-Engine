package com.lobsterchops.redshell.assets.catalog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Declarative registry of every asset a game uses: logical IDs to paths and types, plus named groups. */
public final class AssetCatalog {

    private final Map<String, AssetEntry> entries = new LinkedHashMap<>();
    private final Map<String, AssetGroup> groups = new LinkedHashMap<>();

    /** Registers an asset. Duplicate IDs are rejected. */
    public AssetCatalog register(String id, String path, AssetType type) {
        if (id == null || id.isEmpty() || path == null || path.isEmpty() || type == null) {
            throw new IllegalArgumentException("id, path and type are required");
        }
        if (entries.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate asset id: " + id);
        }
        entries.put(id, new AssetEntry(id, path, type));
        return this;
    }

    /** Defines a group of already registered assets. Unknown IDs and duplicate group names are rejected. */
    public AssetGroup defineGroup(String name, String... ids) {
        if (groups.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate asset group: " + name);
        }
        List<String> list = Arrays.asList(ids);
        for (int i = 0; i < list.size(); i++) {
            if (!entries.containsKey(list.get(i))) {
                throw new IllegalArgumentException("Group '" + name + "' references unknown asset id: " + list.get(i));
            }
        }
        AssetGroup group = new AssetGroup(name, new ArrayList<>(list));
        groups.put(name, group);
        return group;
    }

    public boolean has(String id) {
        return entries.containsKey(id);
    }

    public AssetEntry entry(String id) {
        AssetEntry entry = entries.get(id);
        if (entry == null) {
            throw new IllegalArgumentException("Unknown asset id: " + id);
        }
        return entry;
    }

    public AssetGroup group(String name) {
        AssetGroup group = groups.get(name);
        if (group == null) {
            throw new IllegalArgumentException("Unknown asset group: " + name);
        }
        return group;
    }

    public Collection<AssetEntry> entries() {
        return Collections.unmodifiableCollection(entries.values());
    }

    public Collection<AssetGroup> groups() {
        return Collections.unmodifiableCollection(groups.values());
    }
}