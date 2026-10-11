package com.lobsterchops.sandbox;

import com.lobsterchops.redshell.ecs.World;
import com.lobsterchops.redshell.ecs.query.Query;
import com.lobsterchops.redshell.ecs.system.EntitySystem;
import com.lobsterchops.redshell.entity.Entity;
import com.lobsterchops.redshell.entity.component.SpriteComponent;
import com.lobsterchops.redshell.entity.component.TransformComponent;
import com.lobsterchops.redshell.entity.component.VelocityComponent;
import java.util.List;

/** Walks hazards back and forth between their patrol limits. */
final class PatrolSystem extends EntitySystem {

    private final Query query = Query.all(TransformComponent.class, VelocityComponent.class, HazardComponent.class);

    PatrolSystem() {
        super(90);
    }

    @Override
    public void update(World world, float deltaTime) {
        List<Entity> hazards = world.query(query);
        for (int i = 0; i < hazards.size(); i++) {
            Entity hazard = hazards.get(i);
            HazardComponent patrol = hazard.get(HazardComponent.class);
            TransformComponent transform = hazard.get(TransformComponent.class);
            VelocityComponent velocity = hazard.get(VelocityComponent.class);
            if (transform.x <= patrol.minX) {
                velocity.vx = patrol.speed;
            } else if (transform.x >= patrol.maxX || velocity.vx == 0f) {
                velocity.vx = -patrol.speed;
            }
            SpriteComponent sprite = hazard.get(SpriteComponent.class);
            if (sprite != null) {
                sprite.flipX = velocity.vx < 0f;
            }
        }
    }
}