package com.lobsterchops.arena;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.lobsterchops.redshell.ecs.World;
import com.lobsterchops.redshell.ecs.query.Query;
import com.lobsterchops.redshell.ecs.system.EntitySystem;
import com.lobsterchops.redshell.entity.Entity;
import com.lobsterchops.redshell.entity.EntityTag;
import com.lobsterchops.redshell.entity.component.ColliderComponent;
import com.lobsterchops.redshell.entity.component.SpriteComponent;
import com.lobsterchops.redshell.entity.component.TransformComponent;
import com.lobsterchops.redshell.entity.component.VelocityComponent;
import com.lobsterchops.redshell.entity.prefab.Prefab;
import com.lobsterchops.redshell.input.InputService;
import com.lobsterchops.redshell.rendering.layer.RenderLayer;
import java.util.List;

/** Fires a bullet from the player in the arrow-key direction, at most once per fire interval. */
final class ShootingSystem extends EntitySystem {

    private final InputService input;
    private final TextureRegion bulletRegion;
    private final Query query = Query.all(TransformComponent.class).tagged(EntityTag.PLAYER);
    private float cooldown;

    ShootingSystem(InputService input, TextureRegion bulletRegion) {
        super(5);
        this.input = input;
        this.bulletRegion = bulletRegion;
    }

    @Override
    public void update(World world, float deltaTime) {
        cooldown -= deltaTime;
        float dx = (input.held(ArenaAction.FIRE_RIGHT) ? 1f : 0f) - (input.held(ArenaAction.FIRE_LEFT) ? 1f : 0f);
        float dy = (input.held(ArenaAction.FIRE_UP) ? 1f : 0f) - (input.held(ArenaAction.FIRE_DOWN) ? 1f : 0f);
        if (cooldown > 0f || (dx == 0f && dy == 0f)) {
            return;
        }
        List<Entity> players = world.query(query);
        if (players.isEmpty()) {
            return;
        }
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        TransformComponent origin = players.get(0).get(TransformComponent.class);
        final float x = origin.x;
        final float y = origin.y;
        final float vx = dx / length * ArenaConfig.BULLET_SPEED;
        final float vy = dy / length * ArenaConfig.BULLET_SPEED;
        world.commands().spawn(new Prefab() {
            @Override
            public void build(Entity bullet) {
                bullet.add(new TransformComponent(x, y));
                VelocityComponent velocity = bullet.add(new VelocityComponent());
                velocity.vx = vx;
                velocity.vy = vy;
                SpriteComponent sprite = bullet.add(new SpriteComponent(bulletRegion, 8f, 8f, RenderLayer.ACTORS));
                sprite.red = 1f;
                sprite.green = 0.9f;
                sprite.blue = 0.3f;
                ColliderComponent collider = ColliderComponent.circle(4f, ArenaLayers.BULLET);
                collider.trigger = true;
                bullet.add(collider);
                bullet.add(new Bullet());
            }
        });
        cooldown = ArenaConfig.FIRE_INTERVAL;
    }
}