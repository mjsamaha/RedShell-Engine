package com.lobsterchops.redshell.events.types;

import com.lobsterchops.redshell.events.Event;

/** Published when the application is paused or resumed by the platform. */
public final class PausedEvent implements Event {

    private final boolean paused;

    public PausedEvent(boolean paused) {
        this.paused = paused;
    }

    public boolean paused() {
        return paused;
    }
}