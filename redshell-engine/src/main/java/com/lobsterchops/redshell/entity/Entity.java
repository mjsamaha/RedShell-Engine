package com.lobsterchops.redshell.entity;

import com.lobsterchops.redshell.entity.component.Component;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Identity plus its component set and tags. No behavior: logic lives in systems. */
public final class Entity {

    private final EntityId id;
    private final Map<Class<?>, Component> components = new HashMap<>();
    private final Set<EntityTag> tags = new HashSet<>();
    private EntityListener listener;
    private boolean alive = true;

    public Entity(EntityId id) {
        this.id = id;
    }

    public EntityId id() {
        return id;
    }

    public boolean isAlive() {
        return alive;
    }

    /** Adds a component, replacing one of the same class, and returns it. */
    public <T extends Component> T add(T component) {
        components.put(component.getClass(), component);
        changed();
        return component;
    }

    @SuppressWarnings("unchecked")
    public <T extends Component> T get(Class<T> type) {
        return (T) components.get(type);
    }

    /** Like {@link #get} but fails with a clear message when the component is missing. */
    public <T extends Component> T require(Class<T> type) {
        T component = get(type);
        if (component == null) {
            throw new IllegalStateException(id + " has no " + type.getSimpleName());
        }
        return component;
    }

    public boolean has(Class<?> type) {
        return components.containsKey(type);
    }

    public void remove(Class<?> type) {
        if (components.remove(type) != null) {
            changed();
        }
    }

    public void addTag(EntityTag tag) {
        if (tags.add(tag)) {
            changed();
        }
    }

    public void removeTag(EntityTag tag) {
        if (tags.remove(tag)) {
            changed();
        }
    }

    /** Read-only view of the components, for tooling such as the debug inspector. */
    public Collection<Component> components() {
        return Collections.unmodifiableCollection(components.values());
    }

    /** Read-only view of the tags. */
    public Set<EntityTag> tags() {
        return Collections.unmodifiableSet(tags);
    }

    public boolean hasTag(EntityTag tag) {
        return tags.contains(tag);
    }

    /** Wires the owning world. Called by the world only. */
    public void setListener(EntityListener listener) {
        this.listener = listener;
    }

    /** Marks the entity dead and detaches it from its world. Called by the world only. */
    public void markDead() {
        alive = false;
        listener = null;
        components.clear();
        tags.clear();
    }

    private void changed() {
        if (listener != null) {
            listener.structureChanged(this);
        }
    }
}