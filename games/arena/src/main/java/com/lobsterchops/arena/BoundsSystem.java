package com.lobsterchops.arena;

import com.lobsterchops.redshell.ecs.World;
import com.lobsterchops.redshell.ecs.query.Query;
import com.lobsterchops.redshell.ecs.system.EntitySystem;
import com.lobsterchops.redshell.entity.Entity;
import com.lobsterchops.redshell.entity.EntityTag;
import com.lobsterchops.redshell.entity.component.TransformComponent;
import java.util.List;

/** Keeps the player inside the arena and removes bullets that leave it. Runs after collision. */
final class BoundsSystem extends EntitySystem {

    private static final float MARGIN = 16f;

    private final Query players = Query.all(TransformComponent.class).tagged(EntityTag.PLAYER);
    private final Query bullets = Query.all(TransformComponent.class, Bullet.class);

    BoundsSystem() {
        super(160);
    }

    @Override
    public void update(World world, float deltaTime) {
        List<Entity> found = world.query(players);
        for (int i = 0; i < found.size(); i++) {
            TransformComponent t = found.get(i).get(TransformComponent.class);
            t.x = Math.max(MARGIN, Math.min(ArenaConfig.WIDTH - MARGIN, t.x));
            t.y = Math.max(MARGIN, Math.min(ArenaConfig.HEIGHT - MARGIN, t.y));
        }
        List<Entity> shots = world.query(bullets);
        for (int i = 0; i < shots.size(); i++) {
            TransformComponent t = shots.get(i).get(TransformComponent.class);
            if (t.x < 0f || t.y < 0f || t.x > ArenaConfig.WIDTH || t.y > ArenaConfig.HEIGHT) {
                world.commands().destroy(shots.get(i));
            }
        }
    }
}