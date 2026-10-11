package com.lobsterchops.redshell.scene.transition;

import com.lobsterchops.redshell.rendering.Renderer2D;

/** Switches scenes immediately with no visual effect. */
public final class InstantTransition implements Transition {

    @Override
    public void update(float deltaTime) {
    }

    @Override
    public boolean reachedMidpoint() {
        return true;
    }

    @Override
    public boolean isFinished() {
        return true;
    }

    @Override
    public void render(Renderer2D renderer) {
    }

    @Override
    public Transition copy() {
        return new InstantTransition();
    }
}