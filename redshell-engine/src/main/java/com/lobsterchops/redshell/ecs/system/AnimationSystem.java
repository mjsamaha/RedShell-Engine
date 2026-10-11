package com.lobsterchops.redshell.ecs.system;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.lobsterchops.redshell.ecs.World;
import com.lobsterchops.redshell.ecs.query.Query;
import com.lobsterchops.redshell.entity.Entity;
import com.lobsterchops.redshell.entity.component.AnimationComponent;
import com.lobsterchops.redshell.entity.component.SpriteComponent;
import java.util.List;

/** Advances animation controllers and copies the current frame into the entity's sprite, if it has one. */
public final class AnimationSystem extends EntitySystem {

    private final Query query = Query.all(AnimationComponent.class);

    public AnimationSystem() {
        super(SystemPriority.ANIMATION);
    }

    @Override
    public void update(World world, float deltaTime) {
        List<Entity> entities = world.query(query);
        for (int i = 0; i < entities.size(); i++) {
            Entity entity = entities.get(i);
            AnimationComponent animation = entity.get(AnimationComponent.class);
            animation.controller.update(deltaTime * animation.speed);
            SpriteComponent sprite = entity.get(SpriteComponent.class);
            TextureRegion frame = animation.controller.currentFrame();
            if (sprite != null && frame != null) {
                sprite.region = frame;
            }
        }
    }
}