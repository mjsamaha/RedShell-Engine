package com.lobsterchops.arena;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
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
import com.lobsterchops.redshell.rendering.layer.RenderLayer;

/** Spawns chasers at a random edge of the arena, faster and more often the longer the run lasts. */
final class SpawnSystem extends EntitySystem {

    private static final int MAX_ENEMIES = 40;

    private final TextureRegion enemyRegion;
    private final Query enemies = Query.all(Enemy.class);
    private float elapsed;
    private float timer = 1f;

    SpawnSystem(TextureRegion enemyRegion) {
        super(80);
        this.enemyRegion = enemyRegion;
    }

    @Override
    public void update(World world, float deltaTime) {
        elapsed += deltaTime;
        timer -= deltaTime;
        if (timer > 0f) {
            return;
        }
        timer = Math.max(0.35f, 1.4f - elapsed * 0.015f);
        if (world.query(enemies).size() >= MAX_ENEMIES) {
            return;
        }
        float x;
        float y;
        if (MathUtils.randomBoolean()) {
            x = MathUtils.randomBoolean() ? -16f : ArenaConfig.WIDTH + 16f;
            y = MathUtils.random(0f, ArenaConfig.HEIGHT);
        } else {
            x = MathUtils.random(0f, ArenaConfig.WIDTH);
            y = MathUtils.randomBoolean() ? -16f : ArenaConfig.HEIGHT + 16f;
        }
        final float spawnX = x;
        final float spawnY = y;
        final float speed = MathUtils.random(40f, 60f) + Math.min(40f, elapsed * 0.5f);
        world.commands().spawn(new Prefab() {
            @Override
            public void build(Entity enemy) {
                enemy.add(new TransformComponent(spawnX, spawnY));
                enemy.add(new VelocityComponent());
                enemy.add(new SpriteComponent(enemyRegion, 32f, 32f, RenderLayer.ACTORS));
                ColliderComponent collider = ColliderComponent.box(20f, 20f, ArenaLayers.ENEMY);
                collider.trigger = true;
                enemy.add(collider);
                enemy.add(new Enemy(speed));
                enemy.addTag(EntityTag.ENEMY);
            }
        });
    }
}