package com.lobsterchops.redshell.physics.collision;

/** One collidable in a {@link CollisionWorld}: a shape plus filtering and ownership data. */
public final class Body {

    private final Shape shape;
    private final int layer;
    private final boolean trigger;
    private final boolean immovable;
    private final Object owner;

    public Body(Shape shape, int layer, boolean trigger, boolean immovable, Object owner) {
        this.shape = shape;
        this.layer = layer;
        this.trigger = trigger;
        this.immovable = immovable;
        this.owner = owner;
    }

    public Shape shape() {
        return shape;
    }

    /** Layer index, 0 to 31. */
    public int layer() {
        return layer;
    }

    public boolean trigger() {
        return trigger;
    }

    public boolean immovable() {
        return immovable;
    }

    /** Whatever the caller attached (the ECS adapter attaches the entity). */
    public Object owner() {
        return owner;
    }
}