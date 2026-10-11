package com.lobsterchops.redshell.assets.catalog;

/** One catalog line: a logical ID mapped to a file path and a type. */
public final class AssetEntry {

    private final String id;
    private final String path;
    private final AssetType type;

    AssetEntry(String id, String path, AssetType type) {
        this.id = id;
        this.path = path;
        this.type = type;
    }

    public String id() {
        return id;
    }

    public String path() {
        return path;
    }

    public AssetType type() {
        return type;
    }
}