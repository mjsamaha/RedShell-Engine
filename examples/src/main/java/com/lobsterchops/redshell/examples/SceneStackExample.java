package com.lobsterchops.redshell.examples;

import com.badlogic.gdx.graphics.Color;
import com.lobsterchops.redshell.assets.catalog.AssetGroup;
import com.lobsterchops.redshell.scene.Scene;
import com.lobsterchops.redshell.scene.SceneFlags;
import com.lobsterchops.redshell.scene.transition.FadeTransition;

/**
 * The scene stack: SPACE pushes an overlay scene on top (the scene below stays visible but frozen), SPACE again pops
 * it. Scenes are single use, so each push creates a new instance.
 */
final class SceneStackExample extends ExampleGame {

    @Override
    public String name() {
        return "Example: scene stack";
    }

    @Override
    public Scene createInitialScene() {
        return new BaseScene(group());
    }

    private static final class BaseScene extends Scene {
        private final AssetGroup group;
        private float seconds;

        BaseScene(AssetGroup group) {
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
            seconds += deltaTime;
            if (context().input().pressed(ExampleAction.PRIMARY)) {
                context().scenes().push(new OverlayScene(), new FadeTransition(0.2f));
            }
        }

        @Override
        protected void onRender(float deltaTime) {
            // The bar grows only while this scene is updating, so it visibly freezes under the overlay.
            context().renderer().fillRect(60f, 150f, 40f + (seconds * 40f) % 480f, 60f, Color.SKY);
        }
    }

    private static final class OverlayScene extends Scene {
        @Override
        public SceneFlags flags() {
            return SceneFlags.OVERLAY;
        }

        @Override
        protected void onUpdate(float deltaTime) {
            if (context().input().pressed(ExampleAction.PRIMARY)) {
                context().scenes().pop(new FadeTransition(0.2f));
            }
        }

        @Override
        protected void onRender(float deltaTime) {
            context().renderer().fillRect(160f, 100f, 320f, 160f, new Color(0f, 0f, 0f, 0.7f));
        }
    }
}