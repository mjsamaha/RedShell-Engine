package com.lobsterchops.redshell.assets;

/** Typed reference to a catalog asset. Resolves through the service; holders never dispose the asset. */
public final class AssetHandle<T> {

    private final AssetService service;
    private final String id;
    private final Class<T> type;

    public AssetHandle(AssetService service, String id, Class<T> type) {
        this.service = service;
        this.id = id;
        this.type = type;
    }

    public String id() {
        return id;
    }

    public boolean isLoaded() {
        return service.isLoaded(id);
    }

    public T get() {
        return service.get(id, type);
    }
}