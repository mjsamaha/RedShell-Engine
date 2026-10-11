package com.lobsterchops.redshell.events.types;

import com.lobsterchops.redshell.events.Event;

/** Published when the window (or browser canvas) size changes. */
public final class WindowResizedEvent implements Event {

    private final int width;
    private final int height;

    public WindowResizedEvent(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }
}