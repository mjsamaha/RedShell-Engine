package com.lobsterchops.redshell.util.timer;

/** Runs an action once after a delay. Driven by calling {@link #update(float)}. */
public final class DelayedAction {

    private final Runnable action;
    private float remaining;
    private boolean done;

    public DelayedAction(float delaySeconds, Runnable action) {
        if (delaySeconds < 0f) {
            throw new IllegalArgumentException("delay must not be negative: " + delaySeconds);
        }
        if (action == null) {
            throw new IllegalArgumentException("action must not be null");
        }
        this.remaining = delaySeconds;
        this.action = action;
    }

    /** Advances the delay and runs the action when it elapses. Returns true if the action ran. */
    public boolean update(float deltaSeconds) {
        if (done) {
            return false;
        }
        remaining -= Math.max(0f, deltaSeconds);
        if (remaining > 0f) {
            return false;
        }
        done = true;
        action.run();
        return true;
    }

    public void cancel() {
        done = true;
    }

    public boolean isDone() {
        return done;
    }

    public float remaining() {
        return Math.max(0f, remaining);
    }
}
