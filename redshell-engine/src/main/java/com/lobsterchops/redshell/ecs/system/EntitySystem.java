package com.lobsterchops.redshell.ecs.system;

import com.lobsterchops.redshell.ecs.World;
import com.lobsterchops.redshell.rendering.RenderContext;

/** Base class for systems that process a world's entities. Systems run in ascending {@link #priority()} order. */
public abstract class EntitySystem {

    private final int priority;

    protected EntitySystem(int priority) {
        this.priority = priority;
    }

    public final int priority() {
        return priority;
    }

    public void onAdded(World world) {
    }

    public void onRemoved(World world) {
    }

    public void fixedUpdate(World world, float fixedDeltaTime) {
    }

    public void update(World world, float deltaTime) {
    }

    public void render(World world, RenderContext context) {
    }
}