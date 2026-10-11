package com.lobsterchops.redshell.physics.collision;

import com.lobsterchops.redshell.events.Event;
import com.lobsterchops.redshell.entity.Entity;

/** Events raised by the collision system. Entities are the ones that carried the colliders. */
public final class ContactEvents {

    private ContactEvents() {
    }

    /** An entity started overlapping a trigger collider. */
    public static final class TriggerEntered implements Event {
        private final Entity trigger;
        private final Entity other;

        public TriggerEntered(Entity trigger, Entity other) {
            this.trigger = trigger;
            this.other = other;
        }

        public Entity trigger() {
            return trigger;
        }

        public Entity other() {
            return other;
        }
    }

    /** An entity stopped overlapping a trigger collider. */
    public static final class TriggerExited implements Event {
        private final Entity trigger;
        private final Entity other;

        public TriggerExited(Entity trigger, Entity other) {
            this.trigger = trigger;
            this.other = other;
        }

        public Entity trigger() {
            return trigger;
        }

        public Entity other() {
            return other;
        }
    }

    /** Two solid colliders started touching. */
    public static final class SolidContact implements Event {
        private final Entity a;
        private final Entity b;

        public SolidContact(Entity a, Entity b) {
            this.a = a;
            this.b = b;
        }

        public Entity a() {
            return a;
        }

        public Entity b() {
            return b;
        }
    }
}