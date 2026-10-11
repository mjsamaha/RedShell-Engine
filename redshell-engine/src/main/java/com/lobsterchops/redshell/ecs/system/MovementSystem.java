package com.lobsterchops.redshell.ecs.system;

import com.lobsterchops.redshell.ecs.World;
import com.lobsterchops.redshell.ecs.query.Query;
import com.lobsterchops.redshell.entity.Entity;
import com.lobsterchops.redshell.entity.component.TransformComponent;
import com.lobsterchops.redshell.entity.component.VelocityComponent;
import java.util.List;

/** Moves entities by their velocity. */
public final class MovementSystem extends EntitySystem {

    private final Query query = Query.all(TransformComponent.class, VelocityComponent.class);

    public MovementSystem() {
        super(SystemPriority.MOVEMENT);
    }

    @Override
    public void update(World world, float deltaTime) {
        List<Entity> entities = world.query(query);
        for (int i = 0; i < entities.size(); i++) {
            Entity entity = entities.get(i);
            TransformComponent transform = entity.get(TransformComponent.class);
            VelocityComponent velocity = entity.get(VelocityComponent.class);
            transform.x += velocity.vx * deltaTime;
            transform.y += velocity.vy * deltaTime;
        }
    }
}