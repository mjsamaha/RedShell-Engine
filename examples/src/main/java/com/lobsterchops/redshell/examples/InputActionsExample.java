package com.lobsterchops.redshell.examples;

import com.badlogic.gdx.graphics.Color;
import com.lobsterchops.redshell.assets.catalog.AssetGroup;
import com.lobsterchops.redshell.input.InputService;
import com.lobsterchops.redshell.scene.Scene;

/**
 * Input actions: the scene never asks "is W down?", it asks "is LEFT held?". Keys come from the game's bindings
 * (see ExampleGame), so they can be remapped without touching this code. SPACE (pressed, one frame) cycles the colour.
 */
final class InputActionsExample extends ExampleGame {

    private static final Color[] COLORS = {Color.CORAL, Color.LIME, Color.SKY, Color.GOLD};

    @Override
    public String name() {
        return "Example: input actions";
    }

    @Override
    public Scene createInitialScene() {
        return new InputScene(group());
    }

    private static final class InputScene extends Scene {
        private final AssetGroup group;
        private float x = 320f;
        private float y = 180f;
        private int color;

        InputScene(AssetGroup group) {
            this.group = group;
        }

        @Override
        public AssetGroup assets() {
            return group;
        }

        @Override
        protected void onEnter() {
            context().renderer().camera().setPosition(320f, 180f);
        }

        @Override
        protected void onUpdate(float deltaTime) {
            InputService input = context().input();
            float speed = 160f * deltaTime;
            x += ((input.held(ExampleAction.RIGHT) ? 1f : 0f) - (input.held(ExampleAction.LEFT) ? 1f : 0f)) * speed;
            y += ((input.held(ExampleAction.UP) ? 1f : 0f) - (input.held(ExampleAction.DOWN) ? 1f : 0f)) * speed;
            if (input.pressed(ExampleAction.PRIMARY)) {
                color = (color + 1) % COLORS.length;
            }
        }

        @Override
        protected void onRender(float deltaTime) {
            context().renderer().fillRect(x - 16f, y - 16f, 32f, 32f, COLORS[color]);
        }
    }
}