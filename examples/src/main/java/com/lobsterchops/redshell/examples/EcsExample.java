package com.lobsterchops.redshell.examples;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.lobsterchops.redshell.assets.catalog.AssetGroup;
import com.lobsterchops.redshell.ecs.World;
import com.lobsterchops.redshell.ecs.query.Query;
import com.lobsterchops.redshell.ecs.system.EntitySystem;
import com.lobsterchops.redshell.ecs.system.RenderSystem;
import com.lobsterchops.redshell.ecs.system.SystemPriority;
import com.lobsterchops.redshell.entity.Entity;
import com.lobsterchops.redshell.entity.component.Component;
import com.lobsterchops.redshell.entity.component.SpriteComponent;
import com.lobsterchops.redshell.entity.component.TransformComponent;
import com.lobsterchops.redshell.rendering.layer.RenderLayer;
import com.lobsterchops.redshell.scene.Scene;
import java.util.List;

/**
 * Entities, components, systems and the command buffer: coins spin (Spin component and system) and expire after a
 * few seconds (Lifetime). Systems never destroy entities while iterating; they queue it on {@code world.commands()}.
 */
final class EcsExample extends ExampleGame {

    @Override
    public String name() {
        return "Example: ECS";
    }

    @Override
    public Scene createInitialScene() {
        return new EcsScene(group());
    }

    /** Data only: how fast an entity turns. */
    private static final class Spin implements Component {
        final float degreesPerSecond;

        Spin(float degreesPerSecond) {
            this.degreesPerSecond = degreesPerSecond;
        }
    }

    /** Data only: seconds left before the entity is removed. */
    private static final class Lifetime implements Component {
        float secondsLeft;

        Lifetime(float secondsLeft) {
            this.secondsLeft = secondsLeft;
        }
    }

    /** Behaviour only: finds every entity with a transform and a Spin and turns it. */
    private static final class SpinSystem extends EntitySystem {
        private final Query query = Query.all(TransformComponent.class, Spin.class);

        SpinSystem() {
            super(SystemPriority.MOVEMENT);
        }

        @Override
        public void update(World world, float deltaTime) {
            List<Entity> entities = world.query(query);
            for (int i = 0; i < entities.size(); i++) {
                Entity e = entities.get(i);
                e.get(TransformComponent.class).rotationDegrees += e.get(Spin.class).degreesPerSecond * deltaTime;
            }
        }
    }

    private static final class LifetimeSystem extends EntitySystem {
        private final Query query = Query.all(Lifetime.class);

        LifetimeSystem() {
            super(SystemPriority.LIFETIME);
        }

        @Override
        public void update(World world, float deltaTime) {
            List<Entity> entities = world.query(query);
            for (int i = 0; i < entities.size(); i++) {
                Lifetime lifetime = entities.get(i).get(Lifetime.class);
                lifetime.secondsLeft -= deltaTime;
                if (lifetime.secondsLeft <= 0f) {
                    world.commands().destroy(entities.get(i));
                }
            }
        }
    }

    private static final class EcsScene extends Scene {
        private final AssetGroup group;

        EcsScene(AssetGroup group) {
            this.group = group;
        }

        @Override
        public AssetGroup assets() {
            return group;
        }

        @Override
        protected void onEnter() {
            context().renderer().camera().setPosition(320f, 180f);
            TextureRegion coin = new TextureRegion(context().assets().get(COIN, Texture.class));
            for (int i = 0; i < 40; i++) {
                Entity e = world().createEntity();
                e.add(new TransformComponent(40f + (i % 10) * 60f, 60f + (i / 10) * 70f));
                e.add(new SpriteComponent(coin, 32f, 32f, RenderLayer.ACTORS));
                e.add(new Spin(60f + i * 9f));
                e.add(new Lifetime(2f + i * 0.25f));
            }
            world().addSystem(new SpinSystem());
            world().addSystem(new LifetimeSystem());
            world().addSystem(new RenderSystem());
        }
    }
}