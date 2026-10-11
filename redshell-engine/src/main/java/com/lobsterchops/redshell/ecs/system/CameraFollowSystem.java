package com.lobsterchops.redshell.ecs.system;

import com.lobsterchops.redshell.ecs.World;
import com.lobsterchops.redshell.ecs.query.Query;
import com.lobsterchops.redshell.entity.Entity;
import com.lobsterchops.redshell.entity.EntityTag;
import com.lobsterchops.redshell.entity.component.TransformComponent;
import com.lobsterchops.redshell.rendering.camera.CameraController;
import java.util.List;

/** Makes a camera controller follow the first entity carrying the given tag. */
public final class CameraFollowSystem extends EntitySystem {

    private final CameraController controller;
    private final Query query;

    public CameraFollowSystem(CameraController controller, EntityTag target) {
        super(SystemPriority.CAMERA);
        this.controller = controller;
        this.query = Query.all(TransformComponent.class).tagged(target);
    }

    @Override
    public void update(World world, float deltaTime) {
        List<Entity> targets = world.query(query);
        if (targets.isEmpty()) {
            return;
        }
        TransformComponent transform = targets.get(0).get(TransformComponent.class);
        controller.follow(transform.worldX(), transform.worldY(), deltaTime);
    }
}