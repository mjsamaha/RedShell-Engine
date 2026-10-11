package com.lobsterchops.redshell.entity.component;

/** Collision shape data, read by {@code physics.CollisionSystem}. */
public final class ColliderComponent implements Component {

    /** The collision shape. */
    public enum Shape {
        RECTANGLE,
        CIRCLE
    }

    public Shape shape = Shape.RECTANGLE;
    public float width;
    public float height;
    public float radius;
    /** Shape centre relative to the transform. */
    public float offsetX;
    public float offsetY;
    /** Collision layer index, 0 to 31. */
    public int layer;
    /** Triggers detect overlap and raise events but never push anything. */
    public boolean trigger;
    /** Immovable colliders (walls) are never pushed by solid contacts. */
    public boolean immovable;

    public static ColliderComponent box(float width, float height, int layer) {
        ColliderComponent collider = new ColliderComponent();
        collider.width = width;
        collider.height = height;
        collider.layer = layer;
        return collider;
    }

    public static ColliderComponent circle(float radius, int layer) {
        ColliderComponent collider = new ColliderComponent();
        collider.shape = Shape.CIRCLE;
        collider.radius = radius;
        collider.layer = layer;
        return collider;
    }
}