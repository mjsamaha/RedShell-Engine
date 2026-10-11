package com.lobsterchops.redshell.entity.prefab;

import com.lobsterchops.redshell.entity.Entity;

/** A reusable recipe: fills a fresh entity with preset components and tags. */
public interface Prefab {

    void build(Entity entity);
}