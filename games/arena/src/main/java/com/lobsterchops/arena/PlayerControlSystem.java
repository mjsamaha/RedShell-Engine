package com.lobsterchops.arena;

import com.lobsterchops.redshell.ecs.World;
import com.lobsterchops.redshell.ecs.query.Query;
import com.lobsterchops.redshell.ecs.system.EntitySystem;
import com.lobsterchops.redshell.ecs.system.SystemPriority;
import com.lobsterchops.redshell.entity.Entity;
import com.lobsterchops.redshell.entity.EntityTag;
import com.lobsterchops.redshell.entity.component.AnimationComponent;
import com.lobsterchops.redshell.entity.component.SpriteComponent;
import com.lobsterchops.redshell.entity.component.VelocityComponent;
import com.lobsterchops.redshell.input.InputService;
import java.util.List;

/** WASD to velocity, plus the idle or run animation. */
final class PlayerControlSystem extends EntitySystem {

    private final InputService input;
    private final Query query = Query.all(VelocityComponent.class, AnimationComponent.class, SpriteComponent.class)
            .tagged(EntityTag.PLAYER);

    PlayerControlSystem(InputService input) {
        super(SystemPriority.INPUT);
        this.input = input;
    }

    @Override
    public void update(World world, float deltaTime) {
        float dx = (input.held(ArenaAction.MOVE_RIGHT) ? 1f : 0f) - (input.held(ArenaAction.MOVE_LEFT) ? 1f : 0f);
        float dy = (input.held(ArenaAction.MOVE_UP) ? 1f : 0f) - (input.held(ArenaAction.MOVE_DOWN) ? 1f : 0f);
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length > 0f) {
            dx /= length;
            dy /= length;
        }
        List<Entity> players = world.query(query);
        for (int i = 0; i < players.size(); i++) {
            Entity player = players.get(i);
            VelocityComponent velocity = player.get(VelocityComponent.class);
            velocity.vx = dx * ArenaConfig.PLAYER_SPEED;
            velocity.vy = dy * ArenaConfig.PLAYER_SPEED;
            player.get(AnimationComponent.class).controller.play(length > 0f ? "run" : "idle");
            if (dx != 0f) {
                player.get(SpriteComponent.class).flipX = dx < 0f;
            }
        }
    }
}