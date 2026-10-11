package com.lobsterchops.redshell.ecs.query;

import com.lobsterchops.redshell.entity.Entity;
import com.lobsterchops.redshell.entity.EntityTag;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Selects entities that have all required components and tags and none of the excluded components. Immutable. */
public final class Query {

    private final List<Class<?>> required;
    private final List<Class<?>> excluded;
    private final List<EntityTag> tags;

    private Query(List<Class<?>> required, List<Class<?>> excluded, List<EntityTag> tags) {
        this.required = required;
        this.excluded = excluded;
        this.tags = tags;
    }

    public static Query all(Class<?>... components) {
        return new Query(Arrays.asList(components), new ArrayList<Class<?>>(), new ArrayList<EntityTag>());
    }

    public Query without(Class<?>... components) {
        return new Query(required, Arrays.asList(components), tags);
    }

    public Query tagged(EntityTag... requiredTags) {
        return new Query(required, excluded, Arrays.asList(requiredTags));
    }

    public boolean matches(Entity entity) {
        for (int i = 0; i < required.size(); i++) {
            if (!entity.has(required.get(i))) {
                return false;
            }
        }
        for (int i = 0; i < excluded.size(); i++) {
            if (entity.has(excluded.get(i))) {
                return false;
            }
        }
        for (int i = 0; i < tags.size(); i++) {
            if (!entity.hasTag(tags.get(i))) {
                return false;
            }
        }
        return true;
    }
}