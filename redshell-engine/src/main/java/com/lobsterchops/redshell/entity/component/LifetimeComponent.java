package com.lobsterchops.redshell.entity.component;

/** Seconds until the entity removes itself. */
public final class LifetimeComponent implements Component {

    public float remainingSeconds;

    public LifetimeComponent(float remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }
}