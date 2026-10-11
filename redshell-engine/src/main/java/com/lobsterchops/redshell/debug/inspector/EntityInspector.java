package com.lobsterchops.redshell.debug.inspector;

import com.lobsterchops.redshell.ecs.World;
import com.lobsterchops.redshell.entity.Entity;
import com.lobsterchops.redshell.entity.EntityTag;
import com.lobsterchops.redshell.entity.component.AnimationComponent;
import com.lobsterchops.redshell.entity.component.ColliderComponent;
import com.lobsterchops.redshell.entity.component.Component;
import com.lobsterchops.redshell.entity.component.SpriteComponent;
import com.lobsterchops.redshell.entity.component.TransformComponent;
import com.lobsterchops.redshell.entity.component.VelocityComponent;
import java.util.ArrayList;
import java.util.List;

/**
 * Read-only runtime view of a world's entities: id, tags and component values for the selected entity.
 * It never changes the world. Components it does not know are listed by class name.
 */
public final class EntityInspector {

    private int selected;

    public int selectedIndex() {
        return selected;
    }

    public void next(World world) {
        int count = world.entityCount();
        if (count > 0) {
            selected = (selected + 1) % count;
        }
    }

    public void previous(World world) {
        int count = world.entityCount();
        if (count > 0) {
            selected = (selected - 1 + count) % count;
        }
    }

    /** The selected entity, or null when the world is empty. The selection is clamped if entities were destroyed. */
    public Entity selected(World world) {
        int count = world.entityCount();
        if (count == 0) {
            return null;
        }
        if (selected >= count) {
            selected = count - 1;
        }
        return world.entities().get(selected);
    }

    /** Selects the first entity carrying the tag; returns false if there is none. */
    public boolean selectTagged(World world, EntityTag tag) {
        List<Entity> entities = world.entities();
        for (int i = 0; i < entities.size(); i++) {
            if (entities.get(i).hasTag(tag)) {
                selected = i;
                return true;
            }
        }
        return false;
    }

    /** A header line plus one line per tag set and component of the selected entity. */
    public List<String> lines(World world) {
        List<String> lines = new ArrayList<>();
        Entity entity = selected(world);
        if (entity == null) {
            lines.add("Inspector: no entities");
            return lines;
        }
        lines.add("Entity " + (selected + 1) + "/" + world.entityCount() + " " + entity.id());
        if (!entity.tags().isEmpty()) {
            lines.add("  tags " + entity.tags());
        }
        for (Component component : entity.components()) {
            lines.add("  " + describe(component));
        }
        return lines;
    }

    /** One line describing a component's current values. */
    public static String describe(Component component) {
        if (component instanceof TransformComponent) {
            TransformComponent t = (TransformComponent) component;
            return "Transform pos=(" + round(t.x) + ", " + round(t.y) + ") rot=" + round(t.rotationDegrees)
                    + " scale=(" + round(t.scaleX) + ", " + round(t.scaleY) + ")";
        }
        if (component instanceof VelocityComponent) {
            VelocityComponent v = (VelocityComponent) component;
            return "Velocity (" + round(v.vx) + ", " + round(v.vy) + ")";
        }
        if (component instanceof SpriteComponent) {
            SpriteComponent s = (SpriteComponent) component;
            return "Sprite " + round(s.width) + "x" + round(s.height) + " layer=" + s.layer
                    + (s.visible ? "" : " hidden");
        }
        if (component instanceof ColliderComponent) {
            ColliderComponent c = (ColliderComponent) component;
            String size = c.shape == ColliderComponent.Shape.CIRCLE
                    ? "r=" + round(c.radius)
                    : round(c.width) + "x" + round(c.height);
            return "Collider " + c.shape + " " + size + " layer=" + c.layer
                    + (c.trigger ? " trigger" : "") + (c.immovable ? " immovable" : "");
        }
        if (component instanceof AnimationComponent) {
            return "Animation speed=" + round(((AnimationComponent) component).speed);
        }
        return component.getClass().getSimpleName();
    }

    private static String round(float value) {
        return String.valueOf(Math.round(value * 100f) / 100f);
    }
}