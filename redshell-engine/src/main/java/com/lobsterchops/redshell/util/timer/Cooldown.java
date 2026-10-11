package com.lobsterchops.redshell.util.timer;

/** A reusable "can I act again yet?" gate. Driven by calling {@link #update(float)}. */
public final class Cooldown {

    private final float duration;
    private float remaining;

    public Cooldown(float durationSeconds) {
        if (durationSeconds <= 0f) {
            throw new IllegalArgumentException("duration must be positive: " + durationSeconds);
        }
        this.duration = durationSeconds;
    }

    public void update(float deltaSeconds) {
        if (remaining > 0f && deltaSeconds > 0f) {
            remaining = Math.max(0f, remaining - deltaSeconds);
        }
    }

    public boolean isReady() {
        return remaining <= 0f;
    }

    /** Starts the cooldown if it is ready. Returns whether it was ready. */
    public boolean tryUse() {
        if (!isReady()) {
            return false;
        }
        remaining = duration;
        return true;
    }

    public void reset() {
        remaining = 0f;
    }

    public float remaining() {
        return remaining;
    }

    public float duration() {
        return duration;
    }
}
