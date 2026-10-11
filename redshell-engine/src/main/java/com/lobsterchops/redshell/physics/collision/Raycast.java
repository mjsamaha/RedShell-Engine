package com.lobsterchops.redshell.physics.collision;

/** Ray against {@link Aabb}/{@link Circle} tests. Use {@link CollisionWorld#raycast} to query a world. */
public final class Raycast {

    private Raycast() {
    }

    /** The nearest point where a ray meets a body. */
    public static final class Hit {
        private final Body body;
        private final float distance;
        private final float x;
        private final float y;
        private final float normalX;
        private final float normalY;

        Hit(Body body, float distance, float x, float y, float normalX, float normalY) {
            this.body = body;
            this.distance = distance;
            this.x = x;
            this.y = y;
            this.normalX = normalX;
            this.normalY = normalY;
        }

        public Body body() {
            return body;
        }

        public float distance() {
            return distance;
        }

        public float x() {
            return x;
        }

        public float y() {
            return y;
        }

        public float normalX() {
            return normalX;
        }

        public float normalY() {
            return normalY;
        }
    }

    /** Direction must be normalised. Returns null when the body is missed or beyond {@code maxDistance}. */
    static Hit hit(Body body, float ox, float oy, float dx, float dy, float maxDistance) {
        Shape shape = body.shape();
        if (shape instanceof Aabb) {
            return box(body, (Aabb) shape, ox, oy, dx, dy, maxDistance);
        }
        if (shape instanceof Circle) {
            return circle(body, (Circle) shape, ox, oy, dx, dy, maxDistance);
        }
        return null;
    }

    private static Hit box(Body body, Aabb box, float ox, float oy, float dx, float dy, float maxDistance) {
        float tMin = 0f;
        float tMax = maxDistance;
        float nx = 0f;
        float ny = 0f;
        for (int axis = 0; axis < 2; axis++) {
            float origin = axis == 0 ? ox : oy;
            float dir = axis == 0 ? dx : dy;
            float min = axis == 0 ? box.minX() : box.minY();
            float max = axis == 0 ? box.maxX() : box.maxY();
            if (Math.abs(dir) < 1e-9f) {
                if (origin < min || origin > max) {
                    return null;
                }
                continue;
            }
            float t1 = (min - origin) / dir;
            float t2 = (max - origin) / dir;
            float entrySign = -1f;
            if (t1 > t2) {
                float swap = t1;
                t1 = t2;
                t2 = swap;
                entrySign = 1f;
            }
            if (t1 > tMin) {
                tMin = t1;
                nx = axis == 0 ? entrySign : 0f;
                ny = axis == 1 ? entrySign : 0f;
            }
            tMax = Math.min(tMax, t2);
            if (tMin > tMax) {
                return null;
            }
        }
        return new Hit(body, tMin, ox + dx * tMin, oy + dy * tMin, nx, ny);
    }

    private static Hit circle(Body body, Circle circle, float ox, float oy, float dx, float dy, float maxDistance) {
        float fx = ox - circle.centerX();
        float fy = oy - circle.centerY();
        float b = fx * dx + fy * dy;
        float c = fx * fx + fy * fy - circle.radius() * circle.radius();
        if (c > 0f && b > 0f) {
            return null;
        }
        float discriminant = b * b - c;
        if (discriminant < 0f) {
            return null;
        }
        float t = Math.max(0f, -b - (float) Math.sqrt(discriminant));
        if (t > maxDistance) {
            return null;
        }
        float hx = ox + dx * t;
        float hy = oy + dy * t;
        float nx = (hx - circle.centerX()) / circle.radius();
        float ny = (hy - circle.centerY()) / circle.radius();
        return new Hit(body, t, hx, hy, nx, ny);
    }
}