package com.lobsterchops.redshell.examples;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.lobsterchops.redshell.assets.catalog.AssetGroup;
import com.lobsterchops.redshell.ecs.system.AnimationSystem;
import com.lobsterchops.redshell.ecs.system.RenderSystem;
import com.lobsterchops.redshell.entity.Entity;
import com.lobsterchops.redshell.entity.component.AnimationComponent;
import com.lobsterchops.redshell.entity.component.SpriteComponent;
import com.lobsterchops.redshell.entity.component.TransformComponent;
import com.lobsterchops.redshell.rendering.animation.Animation2D;
import com.lobsterchops.redshell.rendering.animation.AnimationController;
import com.lobsterchops.redshell.rendering.animation.LoopMode;
import com.lobsterchops.redshell.rendering.layer.RenderLayer;
import com.lobsterchops.redshell.rendering.sprite.SpriteSheet;
import com.lobsterchops.redshell.scene.Scene;

/** Sprites: a tiled floor, an animated sprite sheet, and tint, scale and rotation on a plain sprite. */
final class SpriteExample extends ExampleGame {

    @Override
    public String name() {
        return "Example: sprites";
    }

    @Override
    public Scene createInitialScene() {
        return new SpriteScene(group());
    }

    private static final class SpriteScene extends Scene {
        private final AssetGroup group;

        SpriteScene(AssetGroup group) {
            this.group = group;
        }

        @Override
        public AssetGroup assets() {
            return group;
        }

        @Override
        protected void onEnter() {
            context().renderer().camera().setPosition(320f, 180f);
            TextureRegion tile = new TextureRegion(context().assets().get(TILE, Texture.class));
            for (int x = 0; x < 20; x++) {
                for (int y = 0; y < 12; y++) {
                    Entity floor = world().createEntity();
                    floor.add(new TransformComponent(x * 32f + 16f, y * 32f + 16f));
                    floor.add(new SpriteComponent(tile, 32f, 32f, RenderLayer.BACKGROUND));
                }
            }
            // A sprite sheet is cut into frames; an AnimationController picks the frame for the current state.
            SpriteSheet sheet = SpriteSheet.fromAsset(context().assets(), PLAYER, 32, 32);
            AnimationController controller = new AnimationController()
                    .addState("run", new Animation2D("run", sheet.row(1, 4), 0.1f, LoopMode.LOOP));
            controller.play("run");
            Entity runner = world().createEntity();
            runner.add(new TransformComponent(200f, 180f));
            runner.add(new SpriteComponent(controller.currentFrame(), 64f, 64f, RenderLayer.ACTORS));
            runner.add(new AnimationComponent(controller));

            // Tint, size and rotation are plain fields on the components.
            TextureRegion coin = new TextureRegion(context().assets().get(COIN, Texture.class));
            Entity spinner = world().createEntity();
            TransformComponent transform = spinner.add(new TransformComponent(440f, 180f));
            transform.rotationDegrees = 30f;
            SpriteComponent sprite = spinner.add(new SpriteComponent(coin, 64f, 64f, RenderLayer.ACTORS));
            sprite.red = 1f;
            sprite.green = 0.5f;
            sprite.blue = 0.5f;

            world().addSystem(new AnimationSystem());
            world().addSystem(new RenderSystem());
        }
    }
}