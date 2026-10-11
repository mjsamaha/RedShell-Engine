package com.lobsterchops.redshell.ecs;

import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.lobsterchops.redshell.ecs.query.Query;
import com.lobsterchops.redshell.ecs.system.EntitySystem;
import com.lobsterchops.redshell.ecs.system.LifetimeSystem;
import com.lobsterchops.redshell.ecs.system.MovementSystem;
import com.lobsterchops.redshell.entity.Entity;
import com.lobsterchops.redshell.entity.EntityTag;
import com.lobsterchops.redshell.entity.component.LifetimeComponent;
import com.lobsterchops.redshell.entity.component.TransformComponent;
import com.lobsterchops.redshell.entity.component.VelocityComponent;
import com.lobsterchops.redshell.rendering.layer.LayerSorter;
import com.lobsterchops.redshell.rendering.layer.RenderLayer;
import com.lobsterchops.redshell.rendering.sprite.Sprite2D;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class EcsTest {

    @Test
    void movementMovesEntitiesWithVelocity() {
        World world = new World();
        world.addSystem(new MovementSystem());
        Entity e = world.createEntity();
        e.add(new TransformComponent(0f, 0f));
        e.add(new VelocityComponent(10f, -4f));
        world.update(0.5f);
        assertEquals(5f, e.get(TransformComponent.class).x, 1e-4f);
        assertEquals(-2f, e.get(TransformComponent.class).y, 1e-4f);
    }

    @Test
    void queriesFilterByComponentsAndTags() {
        World world = new World();
        Entity a = world.createEntity();
        a.add(new TransformComponent());
        a.addTag(EntityTag.PLAYER);
        Entity b = world.createEntity();
        b.add(new TransformComponent());
        b.add(new VelocityComponent());
        assertEquals(2, world.query(Query.all(TransformComponent.class)).size());
        assertEquals(1, world.query(Query.all(TransformComponent.class).without(VelocityComponent.class)).size());
        assertEquals(a, world.query(Query.all(TransformComponent.class).tagged(EntityTag.PLAYER)).get(0));
    }

    @Test
    void queryCacheRebuildsOnlyWhenStructureChanges() {
        World world = new World();
        world.createEntity().add(new TransformComponent());
        Query query = Query.all(TransformComponent.class);
        world.query(query);
        int builds = world.queryCache().rebuildCount();
        world.query(query);
        world.query(query);
        assertEquals(builds, world.queryCache().rebuildCount());
        world.createEntity().add(new TransformComponent());
        assertEquals(2, world.query(query).size());
        assertTrue(world.queryCache().rebuildCount() > builds);
    }

    @Test
    void structuralChangeDuringSystemUpdateThrows() {
        World world = new World();
        world.createEntity();
        final List<Throwable> caught = new ArrayList<>();
        world.addSystem(new EntitySystem(10) {
            @Override
            public void update(World w, float dt) {
                try {
                    w.createEntity();
                } catch (IllegalStateException e) {
                    caught.add(e);
                }
            }
        });
        world.update(0.1f);
        assertEquals(1, caught.size());
    }

    @Test
    void commandBufferDefersAndFlushes() {
        final World world = new World();
        final Entity victim = world.createEntity();
        world.addSystem(new EntitySystem(10) {
            @Override
            public void update(World w, float dt) {
                w.commands().destroy(victim);
                w.commands().spawn(entity -> entity.add(new TransformComponent()));
                assertEquals(1, w.entityCount());
            }
        });
        world.update(0.1f);
        assertEquals(1, world.entityCount());
        assertFalse(victim.isAlive());
    }

    @Test
    void lifetimeDestroysExpiredEntities() {
        World world = new World();
        world.addSystem(new LifetimeSystem());
        world.createEntity().add(new LifetimeComponent(0.2f));
        world.update(0.1f);
        assertEquals(1, world.entityCount());
        world.update(0.2f);
        assertEquals(0, world.entityCount());
    }

    @Test
    void layerSorterOrdersByLayerThenZThenLowerY() {
        Sprite2D ui = sprite(RenderLayer.UI, 0, 0);
        Sprite2D bg = sprite(RenderLayer.BACKGROUND, 0, 0);
        Sprite2D high = sprite(RenderLayer.ACTORS, 0, 50);
        Sprite2D low = sprite(RenderLayer.ACTORS, 0, 10);
        List<Sprite2D> list = new ArrayList<>(List.of(ui, high, bg, low));
        LayerSorter.sort(list);
        assertSame(bg, list.get(0));
        assertSame(high, list.get(1));
        assertSame(low, list.get(2));
        assertSame(ui, list.get(3));
    }

    private static Sprite2D sprite(RenderLayer layer, int z, float sortY) {
        Sprite2D s = new Sprite2D();
        s.region = new TextureRegion();
        s.layer = layer;
        s.zIndex = z;
        s.sortY = sortY;
        return s;
    }
}