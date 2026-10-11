package com.lobsterchops.redshell.physics.collision;

/** Narrow-phase overlap tests between {@link Aabb} and {@link Circle} shapes. */
public final class Collisions {

    private Collisions() {
    }

    /** Tests {@code a} against {@code b}; the result's normal points the way {@code a} must move to separate. */
    public static boolean test(Shape a, Shape b, CollisionResult out) {
        if (a instanceof Aabb && b instanceof Aabb) {
            return boxBox((Aabb) a, (Aabb) b, out);
        }
        if (a instanceof Circle && b instanceof Circle) {
            return circleCircle((Circle) a, (Circle) b, out);
        }
        if (a instanceof Aabb && b instanceof Circle) {
            return boxCircle((Aabb) a, (Circle) b, out, false);
        }
        if (a instanceof Circle && b instanceof Aabb) {
            return boxCircle((Aabb) b, (Circle) a, out, true);
        }
        throw new IllegalArgumentException("Unsupported shape pair");
    }

    private static boolean boxBox(Aabb a, Aabb b, CollisionResult out) {
        float dx = a.centerX() - b.centerX();
        float dy = a.centerY() - b.centerY();
        float overlapX = a.halfWidth() + b.halfWidth() - Math.abs(dx);
        float overlapY = a.halfHeight() + b.halfHeight() - Math.abs(dy);
        if (overlapX <= 0f || overlapY <= 0f) {
            out.set(false, 0f, 0f, 0f);
            return false;
        }
        if (overlapX < overlapY) {
            out.set(true, dx < 0f ? -1f : 1f, 0f, overlapX);
        } else {
            out.set(true, 0f, dy < 0f ? -1f : 1f, overlapY);
        }
        return true;
    }

    private static boolean circleCircle(Circle a, Circle b, CollisionResult out) {
        float dx = a.centerX() - b.centerX();
        float dy = a.centerY() - b.centerY();
        float radii = a.radius() + b.radius();
        float distanceSquared = dx * dx + dy * dy;
        if (distanceSquared >= radii * radii) {
            out.set(false, 0f, 0f, 0f);
            return false;
        }
        float distance = (float) Math.sqrt(distanceSquared);
        if (distance < 1e-6f) {
            out.set(true, 1f, 0f, radii);
        } else {
            out.set(true, dx / distance, dy / distance, radii - distance);
        }
        return true;
    }

    /** Tests a box against a circle; {@code circleFirst} says which of them the caller passed as shape A. */
    private static boolean boxCircle(Aabb box, Circle circle, CollisionResult out, boolean circleFirst) {
        float closestX = clamp(circle.centerX(), box.minX(), box.maxX());
        float closestY = clamp(circle.centerY(), box.minY(), box.maxY());
        float dx = closestX - circle.centerX();
        float dy = closestY - circle.centerY();
        float distanceSquared = dx * dx + dy * dy;
        float radius = circle.radius();
        if (distanceSquared >= radius * radius && !(dx == 0f && dy == 0f)) {
            out.set(false, 0f, 0f, 0f);
            return false;
        }
        float nx;
        float ny;
        float depth;
        if (dx == 0f && dy == 0f) {
            // Circle centre is inside the box: leave through the nearest face.
            float left = circle.centerX() - box.minX();
            float right = box.maxX() - circle.centerX();
            float down = circle.centerY() - box.minY();
            float up = box.maxY() - circle.centerY();
            float min = Math.min(Math.min(left, right), Math.min(down, up));
            nx = 0f;
            ny = 0f;
            if (min == left) {
                nx = -1f;
            } else if (min == right) {
                nx = 1f;
            } else if (min == down) {
                ny = -1f;
            } else {
                ny = 1f;
            }
            depth = min + radius;
        } else {
            float distance = (float) Math.sqrt(distanceSquared);
            // Direction from the box's closest point toward the circle centre.
            nx = -dx / distance;
            ny = -dy / distance;
            depth = radius - distance;
        }
        // (nx, ny) moves the circle away from the box; flip it when the box is shape A.
        if (circleFirst) {
            out.set(true, nx, ny, depth);
        } else {
            out.set(true, -nx, -ny, depth);
        }
        return true;
    }

    private static float clamp(float value, float min, float max) {
        return value < min ? min : (value > max ? max : value);
    }
}