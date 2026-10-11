package com.lobsterchops.redshell.examples;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.lobsterchops.redshell.assets.catalog.AssetGroup;
import com.lobsterchops.redshell.ecs.World;
import com.lobsterchops.redshell.ecs.query.Query;
import com.lobsterchops.redshell.ecs.system.EntitySystem;
import com.lobsterchops.redshell.ecs.system.MovementSystem;
import com.lobsterchops.redshell.ecs.system.RenderSystem;
import com.lobsterchops.redshell.ecs.system.SystemPriority;
import com.lobsterchops.redshell.entity.Entity;
import com.lobsterchops.redshell.entity.EntityTag;
import com.lobsterchops.redshell.entity.component.ColliderComponent;
import com.lobsterchops.redshell.entity.component.SpriteComponent;
import com.lobsterchops.redshell.entity.component.TransformComponent;
import com.lobsterchops.redshell.entity.component.VelocityComponent;
import com.lobsterchops.redshell.events.EventListener;
import com.lobsterchops.redshell.input.InputService;
import com.lobsterchops.redshell.physics.CollisionSystem;
import com.lobsterchops.redshell.physics.collision.ContactEvents;
import com.lobsterchops.redshell.physics.layer.CollisionMatrix;
import com.lobsterchops.redshell.rendering.layer.RenderLayer;
import com.lobsterchops.redshell.scene.Scene;
import java.util.List;

/**
 * Collision: a box you steer with the arrows is pushed out of solid walls (immovable colliders) and gets a
 * TriggerEntered/TriggerExited event when it crosses the yellow zone, which never blocks. Press F4 to see bounds.
 */
final class CollisionExample extends ExampleGame {

    private static final int WALL = 0;
    private static final int PLAYER = 1;
    private static final int ZONE = 2;

    @Override
    public String name() {
        return "Example: collision";
    }

    @Override
    public Scene createInitialScene() {
        return new CollisionScene(group());
    }

    private static final class Steering extends EntitySystem {
        private final InputService input;
        private final Query query = Query.all(VelocityComponent.class).tagged(EntityTag.PLAYER);

        Steering(InputService input) {
            super(SystemPriority.INPUT);
            this.input = input;
        }

        @Override
        public void update(World world, float deltaTime) {
            List<Entity> players = world.query(query);
            for (int i = 0; i < players.size(); i++) {
                VelocityComponent v = players.get(i).get(VelocityComponent.class);
                v.vx = ((input.held(ExampleAction.RIGHT) ? 1f : 0f) - (input.held(ExampleAction.LEFT) ? 1f : 0f)) * 140f;
                v.vy = ((input.held(ExampleAction.UP) ? 1f : 0f) - (input.held(ExampleAction.DOWN) ? 1f : 0f)) * 140f;
            }
        }
    }

    private static final class CollisionScene extends Scene {
        private final AssetGroup group;

        CollisionScene(AssetGroup group) {
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
            wall(tile, 320f, 12f, 640f, 24f);
            wall(tile, 320f, 348f, 640f, 24f);
            wall(tile, 12f, 180f, 24f, 360f);
            wall(tile, 628f, 180f, 24f, 360f);
            wall(tile, 320f, 180f, 24f, 160f);

            Entity zone = world().createEntity();
            zone.add(new TransformComponent(480f, 180f));
            SpriteComponent zoneSprite = zone.add(new SpriteComponent(tile, 96f, 96f, RenderLayer.BACKGROUND));
            zoneSprite.blue = 0.2f;
            ColliderComponent trigger = ColliderComponent.box(96f, 96f, ZONE);
            trigger.trigger = true;
            trigger.immovable = true;
            zone.add(trigger);

            Entity player = world().createEntity();
            player.add(new TransformComponent(120f, 180f));
            player.add(new VelocityComponent());
            SpriteComponent sprite = player.add(new SpriteComponent(tile, 28f, 28f, RenderLayer.ACTORS));
            sprite.green = 0.4f;
            player.add(ColliderComponent.box(28f, 28f, PLAYER));
            player.addTag(EntityTag.PLAYER);

            subscribe(ContactEvents.TriggerEntered.class, new EventListener<ContactEvents.TriggerEntered>() {
                @Override
                public void onEvent(ContactEvents.TriggerEntered event) {
                    context().log().info("Collision", "entered the zone");
                }
            });
            subscribe(ContactEvents.TriggerExited.class, new EventListener<ContactEvents.TriggerExited>() {
                @Override
                public void onEvent(ContactEvents.TriggerExited event) {
                    context().log().info("Collision", "left the zone");
                }
            });

            world().addSystem(new Steering(context().input()));
            world().addSystem(new MovementSystem());
            world().addSystem(new CollisionSystem(new CollisionMatrix(), context().events()));
            world().addSystem(new RenderSystem());
        }

        private void wall(TextureRegion tile, float x, float y, float width, float height) {
            Entity wall = world().createEntity();
            wall.add(new TransformComponent(x, y));
            wall.add(new SpriteComponent(tile, width, height, RenderLayer.BACKGROUND));
            ColliderComponent collider = ColliderComponent.box(width, height, WALL);
            collider.immovable = true;
            wall.add(collider);
        }
    }
}