package com.lobsterchops.redshell.rendering.animation;

/** Receives {@link AnimationEvent}s from an {@link AnimationController}. */
public interface AnimationEventListener {

    void onAnimationEvent(AnimationEvent event);
}