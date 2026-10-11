package com.lobsterchops.redshell.entity.component;

import com.lobsterchops.redshell.rendering.animation.AnimationController;

/** Current animation state and playback speed. The animation system copies the frame into the sprite. */
public final class AnimationComponent implements Component {

    public final AnimationController controller;
    public float speed = 1f;

    public AnimationComponent(AnimationController controller) {
        this.controller = controller;
    }
}