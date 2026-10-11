package com.lobsterchops.redshell.physics;

import com.lobsterchops.redshell.ecs.World;
import com.lobsterchops.redshell.ecs.query.Query;
import com.lobsterchops.redshell.ecs.system.EntitySystem;
import com.lobsterchops.redshell.entity.Entity;
import com.lobsterchops.redshell.entity.component.ColliderComponent;
import com.lobsterchops.redshell.entity.component.TransformComponent;
import com.lobsterchops.redshell.entity.component.VelocityComponent;
import com.lobsterchops.redshell.events.EventBus;
import com.lobsterchops.redshell.physics.collision.Aabb;
import com.lobsterchops.redshell.physics.collision.Body;
import com.lobsterchops.redshell.physics.collision.Circle;
import com.lobsterchops.redshell.physics.collision.CollisionResult;
import com.lobsterchops.redshell.physics.collision.CollisionWorld;
import com.lobsterchops.redshell.physics.collision.Collisions;
import com.lobsterchops.redshell.physics.collision.ContactEvents;
import com.lobsterchops.redshell.physics.collision.PairVisitor;
import com.lobsterchops.redshell.physics.collision.Raycast;
import com.lobsterchops.redshell.physics.collision.Shape;
import com.lobsterchops.redshell.physics.layer.CollisionMatrix;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds a {@link CollisionWorld} from every entity with a transform and a collider, pushes solid
 * colliders apart, and publishes trigger and contact events when pairs start or stop overlapping.
 *
 * <p>It runs one resolution pass per frame and does not sweep, so an entity must not move further than its own
 * size in one step; clamp velocities or the frame time for fast entities. Colliders are assumed to have no
 * rotated or scaled parent. One instance belongs to one scene's {@link World}.
 */
public final class CollisionSystem extends EntitySystem implements PhysicsService {

    /** Runs after movement and before animation and rendering. */
    public static final int PRIORITY = 150;

    private static final float CELL_SIZE = 64f;

    private final CollisionMatrix matrix;
    private final EventBus events;
    private final Query query = Query.all(TransformComponent.class, ColliderComponent.class);
    private final CollisionWorld collisionWorld = new CollisionWorld(CELL_SIZE);
    private final CollisionResult result = new CollisionResult();
    private final Map<Long, Contact> previous = new HashMap<>();
    private final Map<Long, Contact> current = new HashMap<>();
    private final PairVisitor visitor = new PairVisitor() {
        @Override
        public void visit(Body a, Body b) {
            handle(a, b);
        }
    };

    public CollisionSystem(CollisionMatrix matrix, EventBus events) {
        super(PRIORITY);
        this.matrix = matrix;
        this.events = events;
    }

    @Override
    public void update(World world, float deltaTime) {
        List<Entity> entities = world.query(query);
        collisionWorld.clear();
        for (int i = 0; i < entities.size(); i++) {
            Entity entity = entities.get(i);
            ColliderComponent collider = entity.get(ColliderComponent.class);
            TransformComponent transform = entity.get(TransformComponent.class);
            float cx = transform.worldX() + collider.offsetX;
            float cy = transform.worldY() + collider.offsetY;
            Shape shape = collider.shape == ColliderComponent.Shape.CIRCLE
                    ? new Circle(cx, cy, collider.radius)
                    : new Aabb(cx, cy, collider.width, collider.height);
            collisionWorld.add(new Body(shape, collider.layer, collider.trigger, collider.immovable, entity));
        }
        current.clear();
        collisionWorld.pairs(matrix, visitor);
        publishChanges();
        Map<Long, Contact> swap = new HashMap<>(current);
        previous.clear();
        previous.putAll(swap);
    }

    private void handle(Body first, Body second) {
        Entity a = (Entity) first.owner();
        Entity b = (Entity) second.owner();
        if (!Collisions.test(first.shape(), second.shape(), result)) {
            return;
        }
        boolean trigger = first.trigger() || second.trigger();
        if (!trigger) {
            resolve(first, second, a, b);
        }
        long key = a.id().value() < b.id().value()
                ? ((long) a.id().value() << 32) | (b.id().value() & 0xFFFFFFFFL)
                : ((long) b.id().value() << 32) | (a.id().value() & 0xFFFFFFFFL);
        Entity triggerEntity = first.trigger() ? a : b;
        Entity other = first.trigger() ? b : a;
        current.put(key, new Contact(trigger ? triggerEntity : a, trigger ? other : b, trigger));
    }

    private void resolve(Body first, Body second, Entity a, Entity b) {
        if (first.immovable() && second.immovable()) {
            return;
        }
        float nx = result.normalX();
        float ny = result.normalY();
        float depth = result.depth();
        float shareA = first.immovable() ? 0f : (second.immovable() ? 1f : 0.5f);
        float shareB = 1f - shareA;
        push(first, a, nx * depth * shareA, ny * depth * shareA, nx, ny);
        push(second, b, -nx * depth * shareB, -ny * depth * shareB, -nx, -ny);
    }

    private void push(Body body, Entity entity, float dx, float dy, float nx, float ny) {
        if (dx == 0f && dy == 0f) {
            return;
        }
        TransformComponent transform = entity.get(TransformComponent.class);
        transform.x += dx;
        transform.y += dy;
        body.shape().setCenter(body.shape().centerX() + dx, body.shape().centerY() + dy);
        VelocityComponent velocity = entity.get(VelocityComponent.class);
        if (velocity != null) {
            float into = velocity.vx * nx + velocity.vy * ny;
            if (into < 0f) {
                velocity.vx -= into * nx;
                velocity.vy -= into * ny;
            }
        }
    }

    private void publishChanges() {
        for (Map.Entry<Long, Contact> entry : current.entrySet()) {
            if (!previous.containsKey(entry.getKey())) {
                Contact c = entry.getValue();
                events.publish(c.trigger ? new ContactEvents.TriggerEntered(c.first, c.second)
                        : new ContactEvents.SolidContact(c.first, c.second));
            }
        }
        List<Contact> ended = new ArrayList<>();
        for (Map.Entry<Long, Contact> entry : previous.entrySet()) {
            if (!current.containsKey(entry.getKey()) && entry.getValue().trigger) {
                ended.add(entry.getValue());
            }
        }
        for (int i = 0; i < ended.size(); i++) {
            Contact c = ended.get(i);
            if (c.first.isAlive() && c.second.isAlive()) {
                events.publish(new ContactEvents.TriggerExited(c.first, c.second));
            }
        }
    }

    @Override
    public Raycast.Hit raycast(float originX, float originY, float directionX, float directionY,
                               float maxDistance, int layerMask) {
        return collisionWorld.raycast(originX, originY, directionX, directionY, maxDistance, layerMask);
    }

    @Override
    public void overlap(float minX, float minY, float maxX, float maxY, int layerMask, List<Body> out) {
        collisionWorld.query(minX, minY, maxX, maxY, layerMask, out);
    }

    private static final class Contact {
        private final Entity first;
        private final Entity second;
        private final boolean trigger;

        Contact(Entity first, Entity second, boolean trigger) {
            this.first = first;
            this.second = second;
            this.trigger = trigger;
        }
    }
}