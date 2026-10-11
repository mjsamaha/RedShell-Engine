package com.lobsterchops.arena;

import com.lobsterchops.redshell.ecs.World;
import com.lobsterchops.redshell.ecs.query.Query;
import com.lobsterchops.redshell.ecs.system.EntitySystem;
import com.lobsterchops.redshell.entity.Entity;
import com.lobsterchops.redshell.entity.EntityTag;
import com.lobsterchops.redshell.entity.component.SpriteComponent;
import com.lobsterchops.redshell.entity.component.TransformComponent;
import com.lobsterchops.redshell.entity.component.VelocityComponent;
import java.util.List;

/** Points every enemy's velocity at the player. */
final class ChaseSystem extends EntitySystem {

    private final Query players = Query.all(TransformComponent.class).tagged(EntityTag.PLAYER);
    private final Query enemies = Query.all(TransformComponent.class, VelocityComponent.class, Enemy.class);

    ChaseSystem() {
        super(90);
    }

    @Override
    public void update(World world, float deltaTime) {
        List<Entity> found = world.query(players);
        if (found.isEmpty()) {
            return;
        }
        TransformComponent target = found.get(0).get(TransformComponent.class);
        List<Entity> chasers = world.query(enemies);
        for (int i = 0; i < chasers.size(); i++) {
            Entity enemy = chasers.get(i);
            TransformComponent transform = enemy.get(TransformComponent.class);
            VelocityComponent velocity = enemy.get(VelocityComponent.class);
            float dx = target.x - transform.x;
            float dy = target.y - transform.y;
            float length = (float) Math.sqrt(dx * dx + dy * dy);
            if (length > 0.001f) {
                float speed = enemy.get(Enemy.class).speed;
                velocity.vx = dx / length * speed;
                velocity.vy = dy / length * speed;
            }
            SpriteComponent sprite = enemy.get(SpriteComponent.class);
            if (sprite != null) {
                sprite.flipX = velocity.vx < 0f;
            }
        }
    }
}