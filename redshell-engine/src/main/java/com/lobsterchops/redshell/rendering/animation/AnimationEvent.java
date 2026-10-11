package com.lobsterchops.redshell.rendering.animation;

/** A named hook that fires when an animation enters a chosen frame. */
public final class AnimationEvent {

    private final String animationName;
    private final int frameIndex;
    private final String name;

    public AnimationEvent(String animationName, int frameIndex, String name) {
        this.animationName = animationName;
        this.frameIndex = frameIndex;
        this.name = name;
    }

    public String animationName() {
        return animationName;
    }

    public int frameIndex() {
        return frameIndex;
    }

    public String name() {
        return name;
    }
}