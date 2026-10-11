package com.lobsterchops.sandbox;

import com.lobsterchops.redshell.events.Event;

/** Game events published on the engine's event bus. */
public final class SandboxEvents {

    private SandboxEvents() {
    }

    public static final class CoinCollected implements Event {
        private final int collected;
        private final int total;

        public CoinCollected(int collected, int total) {
            this.collected = collected;
            this.total = total;
        }

        public int collected() {
            return collected;
        }

        public int total() {
            return total;
        }
    }

    public static final class PlayerHurt implements Event {
    }
}