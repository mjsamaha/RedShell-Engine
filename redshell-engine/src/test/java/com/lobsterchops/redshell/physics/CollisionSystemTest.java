package com.lobsterchops.redshell.physics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.lobsterchops.redshell.ecs.World;
import com.lobsterchops.redshell.ecs.system.MovementSystem;
import com.lobsterchops.redshell.entity.Entity;
import com.lobsterchops.redshell.entity.component.ColliderComponent;
import com.lobsterchops.redshell.entity.component.TransformComponent;
import com.lobsterchops.redshell.entity.component.VelocityComponent;
import com.lobsterchops.redshell.debug.log.Log;
import com.lobsterchops.redshell.debug.log.LogLevel;
import com.lobsterchops.redshell.events.EventBus;
import com.lobsterchops.redshell.physics.layer.CollisionMatrix;
import org.junit.jupiter.api.Test;

class CollisionSystemTest {

    @Test
    void triggersNeverMoveWhenSolidsPassThrough() {
        World world = new World();
        world.addSystem(new MovementSystem());
        world.addSystem(new CollisionSystem(new CollisionMatrix(), new EventBus(new Log(LogLevel.OFF))));
        Entity coin = world.createEntity();
        coin.add(new TransformComponent(100, 100));
        ColliderComponent c = ColliderComponent.circle(10, 2);
        c.trigger = true;
        c.immovable = true;
        coin.add(c);
        Entity player = world.createEntity();
        player.add(new TransformComponent(60, 100));
        VelocityComponent v = new VelocityComponent();
        v.vx = 110;
        player.add(v);
        player.add(ColliderComponent.box(18, 18, 1));
        for (int i = 0; i < 120; i++) {
            world.update(1 / 60f);
        }
        assertEquals(100f, coin.get(TransformComponent.class).x, 1e-4);
        assertEquals(100f, coin.get(TransformComponent.class).y, 1e-4);
    }

    @Test
    void coinsStayPutAmongWallsAndHazards() {
        com.lobsterchops.redshell.physics.layer.CollisionMatrix m = new CollisionMatrix()
                .setCollides(2, 0, false).setCollides(2, 2, false).setCollides(2, 3, false)
                .setCollides(3, 0, false).setCollides(3, 3, false).setCollides(0, 0, false);
        World world = new World();
        world.addSystem(new MovementSystem());
        world.addSystem(new CollisionSystem(m, new EventBus(new Log(LogLevel.OFF))));
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                if (x == 0 || y == 0 || x == 9 || y == 9) {
                    Entity wall = world.createEntity();
                    wall.add(new TransformComponent(x * 32 + 16, y * 32 + 16));
                    ColliderComponent w = ColliderComponent.box(32, 32, 0);
                    w.immovable = true;
                    wall.add(w);
                }
            }
        }
        Entity[] coins = new Entity[5];
        for (int i = 0; i < coins.length; i++) {
            coins[i] = world.createEntity();
            coins[i].add(new TransformComponent(80 + i * 40, 150));
            ColliderComponent c = ColliderComponent.circle(10, 2);
            c.trigger = true;
            c.immovable = true;
            coins[i].add(c);
        }
        Entity hazard = world.createEntity();
        hazard.add(new TransformComponent(100, 150));
        VelocityComponent hv = new VelocityComponent();
        hv.vx = 50;
        hazard.add(hv);
        ColliderComponent hc = ColliderComponent.box(22, 18, 3);
        hc.trigger = true;
        hazard.add(hc);
        Entity player = world.createEntity();
        player.add(new TransformComponent(60, 150));
        VelocityComponent v = new VelocityComponent();
        player.add(v);
        player.add(ColliderComponent.box(18, 18, 1));
        float[][] dirs = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}, {1, 1}};
        for (int step = 0; step < 600; step++) {
            float[] d = dirs[(step / 60) % dirs.length];
            v.vx = d[0] * 110;
            v.vy = d[1] * 110;
            world.update(1 / 60f);
        }
        for (int i = 0; i < coins.length; i++) {
            assertEquals(80 + i * 40, coins[i].get(TransformComponent.class).x, 1e-4);
            assertEquals(150f, coins[i].get(TransformComponent.class).y, 1e-4);
        }
    }
}