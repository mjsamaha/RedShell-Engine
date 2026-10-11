package com.lobsterchops.redshell.entity;

/** Notified when an entity's structure (components or tags) changes. Implemented by the owning world. */
public interface EntityListener {

    void structureChanged(Entity entity);
}