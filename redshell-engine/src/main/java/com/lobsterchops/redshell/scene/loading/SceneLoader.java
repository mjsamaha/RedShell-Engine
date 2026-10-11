package com.lobsterchops.redshell.scene.loading;

import com.lobsterchops.redshell.assets.AssetService;
import com.lobsterchops.redshell.assets.catalog.AssetGroup;

/** Drives the asynchronous load of one asset group: start it, step it every frame, read progress. */
public final class SceneLoader {

    private final AssetService assets;
    private final AssetGroup group;
    private boolean started;
    private boolean done;
    private boolean handedOver;

    public SceneLoader(AssetService assets, AssetGroup group) {
        this.assets = assets;
        this.group = group;
    }

    /** Queues the group (once) and advances loading. Returns true once everything is loaded. */
    public boolean step() {
        if (!started) {
            started = true;
            assets.load(group);
        }
        if (!done && assets.update() && assets.isLoaded(group)) {
            done = true;
        }
        return done;
    }

    public float progress() {
        return done ? 1f : assets.progress();
    }

    public boolean isDone() {
        return done;
    }

    /** Gives the loaded group's reference to its new owner, so {@link #cancel()} no longer releases it. */
    public void handOver() {
        handedOver = true;
    }

    /** Releases the group's reference if loading started and was never handed over. */
    public void cancel() {
        if (started && !handedOver) {
            handedOver = true;
            assets.unload(group);
        }
    }
}