package com.lobsterchops.redshell.ecs.query;

import com.lobsterchops.redshell.entity.Entity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Remembers each query's matches and rescans only after the world's structure version has changed. */
public final class QueryCache {

    private final Map<Query, Cached> cache = new HashMap<>();
    private int rebuilds;

    public List<Entity> get(Query query, List<Entity> entities, int structureVersion) {
        Cached cached = cache.get(query);
        if (cached == null || cached.version != structureVersion) {
            List<Entity> matches = new ArrayList<>();
            for (int i = 0; i < entities.size(); i++) {
                Entity entity = entities.get(i);
                if (query.matches(entity)) {
                    matches.add(entity);
                }
            }
            cached = new Cached(Collections.unmodifiableList(matches), structureVersion);
            cache.put(query, cached);
            rebuilds++;
        }
        return cached.matches;
    }

    /** How many full scans have happened; useful to verify caching. */
    public int rebuildCount() {
        return rebuilds;
    }

    public void clear() {
        cache.clear();
    }

    private static final class Cached {
        private final List<Entity> matches;
        private final int version;

        Cached(List<Entity> matches, int version) {
            this.matches = matches;
            this.version = version;
        }
    }
}