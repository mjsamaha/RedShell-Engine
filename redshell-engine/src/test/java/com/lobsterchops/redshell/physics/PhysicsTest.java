package com.lobsterchops.redshell.physics;

import static org.junit.jupiter.api.Assertions.*;

import com.lobsterchops.redshell.physics.collision.*;
import com.lobsterchops.redshell.physics.layer.CollisionMatrix;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PhysicsTest {

    @Test
    void aabbOverlapGivesNormalAwayFromOther() {
        CollisionResult r = new CollisionResult();
        assertTrue(Collisions.test(new Aabb(0, 0, 10, 10), new Aabb(8, 0, 10, 10), r));
        assertEquals(2f, r.depth(), 1e-4);
        assertEquals(-1f, r.normalX(), 1e-4);
        assertTrue(Collisions.test(new Aabb(8, 0, 10, 10), new Aabb(0, 0, 10, 10), r));
        assertEquals(1f, r.normalX(), 1e-4);
    }

    @Test
    void separatedShapesDoNotCollide() {
        CollisionResult r = new CollisionResult();
        assertFalse(Collisions.test(new Aabb(0, 0, 10, 10), new Aabb(20, 0, 10, 10), r));
        assertFalse(Collisions.test(new Circle(0, 0, 5), new Circle(20, 0, 5), r));
        assertFalse(Collisions.test(new Circle(0, 0, 5), new Aabb(20, 0, 10, 10), r));
    }

    @Test
    void circleCircleAndMixed() {
        CollisionResult r = new CollisionResult();
        assertTrue(Collisions.test(new Circle(0, 0, 5), new Circle(8, 0, 5), r));
        assertEquals(2f, r.depth(), 1e-4);
        assertEquals(-1f, r.normalX(), 1e-4);
        assertTrue(Collisions.test(new Circle(0, 0, 5), new Aabb(8, 0, 10, 10), r));
        assertEquals(-1f, r.normalX(), 1e-4);
        assertTrue(Collisions.test(new Aabb(8, 0, 10, 10), new Circle(0, 0, 5), r));
        assertEquals(1f, r.normalX(), 1e-4);
    }

    @Test
    void matrixFiltersLayers() {
        CollisionMatrix m = new CollisionMatrix();
        assertTrue(m.collides(1, 2));
        m.setCollides(1, 2, false);
        assertFalse(m.collides(1, 2));
        assertFalse(m.collides(2, 1));
        assertTrue(m.collides(1, 1));
    }

    @Test
    void pairsReportedOnceAndMatrixRespected() {
        CollisionWorld w = new CollisionWorld(16);
        w.add(new Body(new Aabb(0, 0, 20, 20), 0, false, false, null));
        w.add(new Body(new Aabb(10, 0, 20, 20), 1, false, false, null));
        w.add(new Body(new Aabb(500, 500, 20, 20), 0, false, false, null));
        w.rebuild();
        final int[] count = {0};
        w.pairs(new CollisionMatrix(), (a, b) -> count[0]++);
        assertEquals(1, count[0]);
        count[0] = 0;
        w.pairs(new CollisionMatrix().setCollides(0, 1, false), (a, b) -> count[0]++);
        assertEquals(0, count[0]);
    }

    @Test
    void manyBodiesStaySubQuadratic() {
        CollisionWorld w = new CollisionWorld(32);
        for (int i = 0; i < 3000; i++) {
            w.add(new Body(new Aabb((i % 60) * 100, (i / 60) * 100, 20, 20), 0, false, false, null));
        }
        w.rebuild();
        final int[] visits = {0};
        w.pairs(new CollisionMatrix(), (a, b) -> visits[0]++);
        assertEquals(0, visits[0]);
    }

    @Test
    void queryFindsBodiesInRegion() {
        CollisionWorld w = new CollisionWorld(16);
        Body in = new Body(new Aabb(5, 5, 4, 4), 0, false, false, null);
        w.add(in);
        w.add(new Body(new Aabb(300, 300, 4, 4), 0, false, false, null));
        w.rebuild();
        List<Body> out = new ArrayList<>();
        w.query(0, 0, 20, 20, -1, out);
        assertEquals(1, out.size());
        assertSame(in, out.get(0));
    }

    @Test
    void raycastHitsBoxAndCircleAndMisses() {
        CollisionWorld w = new CollisionWorld(16);
        Body box = new Body(new Aabb(50, 0, 10, 10), 0, false, false, null);
        w.add(box);
        w.rebuild();
        Raycast.Hit hit = w.raycast(0, 0, 1, 0, 100, -1);
        assertNotNull(hit);
        assertSame(box, hit.body());
        assertEquals(45f, hit.distance(), 1e-3);
        assertNull(w.raycast(0, 50, 1, 0, 100, -1));

        CollisionWorld c = new CollisionWorld(16);
        c.add(new Body(new Circle(50, 0, 5), 0, false, false, null));
        c.rebuild();
        Raycast.Hit ch = c.raycast(0, 0, 1, 0, 100, -1);
        assertNotNull(ch);
        assertEquals(45f, ch.distance(), 1e-3);
    }
}