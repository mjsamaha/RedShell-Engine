package com.lobsterchops.redshell.entity;

/** Stable identifier of an entity within its world. */
public final class EntityId {

    private final int value;

    public EntityId(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof EntityId && ((EntityId) other).value == value;
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public String toString() {
        return "Entity#" + value;
    }
}