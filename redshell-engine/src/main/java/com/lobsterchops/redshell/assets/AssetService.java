package com.lobsterchops.redshell.assets;

import com.lobsterchops.redshell.assets.catalog.AssetCatalog;
import com.lobsterchops.redshell.assets.catalog.AssetGroup;

/**
 * The single owner of shared resources. Groups are reference counted: every {@link #load} must be
 * balanced by one {@link #unload}, and an asset is freed when its last reference goes.
 * Entities and scenes never dispose assets themselves.
 */
public interface AssetService {

    AssetCatalog catalog();

    /** Queues every asset of the group. Call {@link #update()} each frame to make progress. */
    void load(AssetGroup group);

    /** Releases one reference to every asset of the group. */
    void unload(AssetGroup group);

    /** Advances asynchronous loading. Returns true when nothing is left to load. */
    boolean update();

    /** Fraction of queued work completed, 0 to 1. */
    float progress();

    /** Blocks until the queue is empty. Prefer {@link #update()} on the web. */
    void finishLoading();

    boolean isLoaded(String id);

    boolean isLoaded(AssetGroup group);

    int referenceCount(String id);

    <T> T get(String id, Class<T> type);

    default <T> AssetHandle<T> handle(String id, Class<T> type) {
        return new AssetHandle<>(this, id, type);
    }
}