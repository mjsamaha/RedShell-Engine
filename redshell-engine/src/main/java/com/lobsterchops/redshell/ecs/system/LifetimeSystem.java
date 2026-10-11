package com.lobsterchops.redshell.ecs.system;

import com.lobsterchops.redshell.ecs.World;
import com.lobsterchops.redshell.ecs.query.Query;
import com.lobsterchops.redshell.entity.Entity;
import com.lobsterchops.redshell.entity.component.LifetimeComponent;
import java.util.List;

/** Removes entities whose lifetime has run out, through the command buffer. */
public final class LifetimeSystem extends EntitySystem {

    private final Query query = Query.all(LifetimeComponent.class);

    public LifetimeSystem() {
        super(SystemPriority.LIFETIME);
    }

    @Override
    public void update(World world, float deltaTime) {
        List<Entity> entities = world.query(query);
        for (int i = 0; i < entities.size(); i++) {
            Entity entity = entities.get(i);
            LifetimeComponent lifetime = entity.get(LifetimeComponent.class);
            lifetime.remainingSeconds -= deltaTime;
            if (lifetime.remainingSeconds <= 0f) {
                world.commands().destroy(entity);
            }
        }
    }
}