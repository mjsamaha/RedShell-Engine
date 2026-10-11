package com.lobsterchops.redshell.ecs;

import com.lobsterchops.redshell.ecs.query.Query;
import com.lobsterchops.redshell.ecs.query.QueryCache;
import com.lobsterchops.redshell.ecs.system.EntitySystem;
import com.lobsterchops.redshell.entity.Entity;
import com.lobsterchops.redshell.entity.EntityId;
import com.lobsterchops.redshell.entity.EntityListener;
import com.lobsterchops.redshell.entity.prefab.Prefab;
import com.lobsterchops.redshell.rendering.RenderContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds a scene's entities and systems. Systems run in ascending priority order (ties keep insertion order).
 * While a system runs, creating or destroying entities and adding or removing components is an error:
 * queue those changes on {@link #commands()}, which is applied after each system.
 */
public final class World {

    private final List<Entity> entities = new ArrayList<>();
    private final Map<EntityId, Entity> byId = new HashMap<>();
    private final List<EntitySystem> systems = new ArrayList<>();
    private final EntityCommandBuffer commands = new EntityCommandBuffer();
    private final QueryCache queryCache = new QueryCache();
    private final EntityListener listener = new EntityListener() {
        @Override
        public void structureChanged(Entity entity) {
            requireNotIterating();
            structureVersion++;
        }
    };
    private int nextId = 1;
    private int structureVersion;
    private boolean iterating;
    private boolean disposed;

    public Entity createEntity() {
        requireNotIterating();
        Entity entity = new Entity(new EntityId(nextId++));
        entities.add(entity);
        byId.put(entity.id(), entity);
        entity.setListener(listener);
        structureVersion++;
        return entity;
    }

    /** Creates an entity and fills it using the prefab. */
    public Entity spawn(Prefab prefab) {
        Entity entity = createEntity();
        prefab.build(entity);
        return entity;
    }

    public void destroy(Entity entity) {
        requireNotIterating();
        if (entities.remove(entity)) {
            byId.remove(entity.id());
            entity.markDead();
            structureVersion++;
        }
    }

    public Entity entity(EntityId id) {
        return byId.get(id);
    }

    public int entityCount() {
        return entities.size();
    }

    public List<Entity> entities() {
        return Collections.unmodifiableList(entities);
    }

    /** Entities matching the query. The result is cached until the world's structure changes. */
    public List<Entity> query(Query query) {
        return queryCache.get(query, entities, structureVersion);
    }

    public EntityCommandBuffer commands() {
        return commands;
    }

    public QueryCache queryCache() {
        return queryCache;
    }

    public void addSystem(EntitySystem system) {
        int index = systems.size();
        while (index > 0 && systems.get(index - 1).priority() > system.priority()) {
            index--;
        }
        systems.add(index, system);
        system.onAdded(this);
    }

    public void removeSystem(EntitySystem system) {
        if (systems.remove(system)) {
            system.onRemoved(this);
        }
    }

    public List<EntitySystem> systems() {
        return Collections.unmodifiableList(systems);
    }

    public void fixedUpdate(float fixedDeltaTime) {
        for (int i = 0; i < systems.size(); i++) {
            EntitySystem system = systems.get(i);
            iterating = true;
            try {
                system.fixedUpdate(this, fixedDeltaTime);
            } finally {
                iterating = false;
            }
            commands.flush(this);
        }
    }

    public void update(float deltaTime) {
        for (int i = 0; i < systems.size(); i++) {
            EntitySystem system = systems.get(i);
            iterating = true;
            try {
                system.update(this, deltaTime);
            } finally {
                iterating = false;
            }
            commands.flush(this);
        }
    }

    public void render(RenderContext context) {
        for (int i = 0; i < systems.size(); i++) {
            iterating = true;
            try {
                systems.get(i).render(this, context);
            } finally {
                iterating = false;
            }
        }
    }

    /** Releases every entity and system. The world must not be used afterwards. */
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        for (int i = systems.size() - 1; i >= 0; i--) {
            systems.get(i).onRemoved(this);
        }
        systems.clear();
        for (int i = 0; i < entities.size(); i++) {
            entities.get(i).markDead();
        }
        entities.clear();
        byId.clear();
        queryCache.clear();
        structureVersion++;
    }

    public boolean isDisposed() {
        return disposed;
    }

    private void requireNotIterating() {
        if (iterating) {
            throw new IllegalStateException(
                    "Structural change while a system is running; queue it on World.commands() instead");
        }
    }
}
