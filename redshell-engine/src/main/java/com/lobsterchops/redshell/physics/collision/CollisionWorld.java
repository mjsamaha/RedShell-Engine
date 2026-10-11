package com.lobsterchops.redshell.physics.collision;

import com.badlogic.gdx.utils.IntMap;
import com.lobsterchops.redshell.physics.layer.CollisionMatrix;
import java.util.ArrayList;
import java.util.List;

/**
 * Broad phase on a uniform grid. Bodies are added each frame (or kept between frames and {@link #rebuild}t);
 * pair and region queries only touch bodies in nearby cells, so cost grows with local density, not the square
 * of the body count.
 */
public final class CollisionWorld {

    private final float cellSize;
    private final List<Body> bodies = new ArrayList<>();
    private final IntMap<List<Body>> cells = new IntMap<>();
    private final List<List<Body>> pool = new ArrayList<>();
    private boolean dirty;

    public CollisionWorld(float cellSize) {
        if (cellSize <= 0f) {
            throw new IllegalArgumentException("cellSize must be positive: " + cellSize);
        }
        this.cellSize = cellSize;
    }

    public void add(Body body) {
        bodies.add(body);
        dirty = true;
    }

    public void clear() {
        bodies.clear();
        dirty = true;
    }

    public int size() {
        return bodies.size();
    }

    public List<Body> bodies() {
        return bodies;
    }

    /** Re-buckets every body. Call after bodies moved. */
    public void rebuild() {
        for (IntMap.Entry<List<Body>> entry : cells) {
            entry.value.clear();
            pool.add(entry.value);
        }
        cells.clear();
        for (int i = 0; i < bodies.size(); i++) {
            Body body = bodies.get(i);
            int x0 = cell(body.shape().minX());
            int x1 = cell(body.shape().maxX());
            int y0 = cell(body.shape().minY());
            int y1 = cell(body.shape().maxY());
            for (int cx = x0; cx <= x1; cx++) {
                for (int cy = y0; cy <= y1; cy++) {
                    bucket(cx, cy, true).add(body);
                }
            }
        }
        dirty = false;
    }

    /**
     * Reports every pair of bodies whose bounds overlap and whose layers collide according to the matrix.
     * Each pair is reported once, in the cell holding the corner of the shared bounds.
     */
    public void pairs(CollisionMatrix matrix, PairVisitor visitor) {
        ensureBuilt();
        for (IntMap.Entry<List<Body>> entry : cells) {
            List<Body> list = entry.value;
            int cx = entry.key >> 16;
            int cy = (short) entry.key;
            for (int i = 0; i < list.size(); i++) {
                Body a = list.get(i);
                for (int j = i + 1; j < list.size(); j++) {
                    Body b = list.get(j);
                    if (!matrix.collides(a.layer(), b.layer()) || !boundsOverlap(a.shape(), b.shape())) {
                        continue;
                    }
                    float cornerX = Math.max(a.shape().minX(), b.shape().minX());
                    float cornerY = Math.max(a.shape().minY(), b.shape().minY());
                    if (cell(cornerX) == cx && cell(cornerY) == cy) {
                        visitor.visit(a, b);
                    }
                }
            }
        }
    }

    /** Collects bodies whose bounds overlap the region and whose layer bit is in {@code layerMask}. */
    public void query(float minX, float minY, float maxX, float maxY, int layerMask, List<Body> out) {
        ensureBuilt();
        int x0 = cell(minX);
        int x1 = cell(maxX);
        int y0 = cell(minY);
        int y1 = cell(maxY);
        int start = out.size();
        for (int cx = x0; cx <= x1; cx++) {
            for (int cy = y0; cy <= y1; cy++) {
                List<Body> list = bucket(cx, cy, false);
                if (list == null) {
                    continue;
                }
                for (int i = 0; i < list.size(); i++) {
                    Body body = list.get(i);
                    Shape s = body.shape();
                    if ((layerMask & (1 << body.layer())) != 0
                            && s.maxX() >= minX && s.minX() <= maxX && s.maxY() >= minY && s.minY() <= maxY
                            && !contains(out, start, body)) {
                        out.add(body);
                    }
                }
            }
        }
    }

    /** Casts a ray and returns the nearest hit among bodies on the given layers, or null. */
    public Raycast.Hit raycast(float originX, float originY, float directionX, float directionY,
                               float maxDistance, int layerMask) {
        ensureBuilt();
        float length = (float) Math.sqrt(directionX * directionX + directionY * directionY);
        if (length < 1e-9f) {
            return null;
        }
        float dx = directionX / length;
        float dy = directionY / length;
        float endX = originX + dx * maxDistance;
        float endY = originY + dy * maxDistance;
        List<Body> candidates = new ArrayList<>();
        query(Math.min(originX, endX), Math.min(originY, endY), Math.max(originX, endX), Math.max(originY, endY),
                layerMask, candidates);
        Raycast.Hit best = null;
        for (int i = 0; i < candidates.size(); i++) {
            Raycast.Hit hit = Raycast.hit(candidates.get(i), originX, originY, dx, dy, maxDistance);
            if (hit != null && (best == null || hit.distance() < best.distance())) {
                best = hit;
            }
        }
        return best;
    }

    private void ensureBuilt() {
        if (dirty) {
            rebuild();
        }
    }

    private static boolean contains(List<Body> list, int from, Body body) {
        for (int i = from; i < list.size(); i++) {
            if (list.get(i) == body) {
                return true;
            }
        }
        return false;
    }

    private static boolean boundsOverlap(Shape a, Shape b) {
        return a.minX() < b.maxX() && b.minX() < a.maxX() && a.minY() < b.maxY() && b.minY() < a.maxY();
    }

    private int cell(float coordinate) {
        return (int) Math.floor(coordinate / cellSize);
    }

    private List<Body> bucket(int cx, int cy, boolean create) {
        int key = (cx << 16) | (cy & 0xFFFF);
        List<Body> list = cells.get(key);
        if (list == null && create) {
            list = pool.isEmpty() ? new ArrayList<Body>() : pool.remove(pool.size() - 1);
            cells.put(key, list);
        }
        return list;
    }
}