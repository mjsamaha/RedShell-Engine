package com.lobsterchops.redshell.rendering.animation;

/** How an animation behaves after its last frame. */
public enum LoopMode {
    /** Plays once and holds the last frame. */
    ONCE,
    /** Restarts from the first frame. */
    LOOP,
    /** Plays forward then backward, repeatedly, without repeating the end frames. */
    PING_PONG
}