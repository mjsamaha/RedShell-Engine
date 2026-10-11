package com.lobsterchops.redshell.entity;

/** A label for finding entities, for example PLAYER or ENEMY. Tags with the same name are equal. */
public final class EntityTag {

    public static final EntityTag PLAYER = new EntityTag("PLAYER");
    public static final EntityTag ENEMY = new EntityTag("ENEMY");

    private final String name;

    public EntityTag(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof EntityTag && ((EntityTag) other).name.equals(name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}