package com.lobsterchops.redshell.scene.transition;

import com.lobsterchops.redshell.rendering.Renderer2D;

/**
 * A visual change between scenes. The scene change is applied when {@link #reachedMidpoint()} first
 * becomes true; the transition then keeps running until {@link #isFinished()}.
 */
public interface Transition {

    void update(float deltaTime);

    boolean reachedMidpoint();

    boolean isFinished();

    /** Draws the overlay on top of the scenes. */
    void render(Renderer2D renderer);

    /** A fresh, unstarted transition of the same kind. */
    Transition copy();
}