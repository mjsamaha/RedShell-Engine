package com.lobsterchops.redshell.util.timer;

/** Counts elapsed time. Driven by calling {@link #update(float)} with the game clock's delta. */
public final class Timer {

    private float duration;
    private final boolean repeating;
    private float elapsed;
    private boolean finished;

    public Timer(float durationSeconds, boolean repeating) {
        if (durationSeconds <= 0f) {
            throw new IllegalArgumentException("duration must be positive: " + durationSeconds);
        }
        this.duration = durationSeconds;
        this.repeating = repeating;
    }

    /**
     * Advances the timer.
     *
     * @return true if the timer completed during this update. A repeating timer keeps its overflow
     *         and returns true at most once per call.
     */
    public boolean update(float deltaSeconds) {
        if (finished || deltaSeconds <= 0f) {
            return false;
        }
        elapsed += deltaSeconds;
        if (elapsed < duration) {
            return false;
        }
        if (repeating) {
            elapsed %= duration;
        } else {
            elapsed = duration;
            finished = true;
        }
        return true;
    }

    public void reset() {
        elapsed = 0f;
        finished = false;
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean isRepeating() {
        return repeating;
    }

    public float elapsed() {
        return elapsed;
    }

    public float remaining() {
        return duration - elapsed;
    }

    /** Completion between 0 and 1. */
    public float progress() {
        return elapsed / duration;
    }

    public float duration() {
        return duration;
    }

    public void setDuration(float durationSeconds) {
        if (durationSeconds <= 0f) {
            throw new IllegalArgumentException("duration must be positive: " + durationSeconds);
        }
        this.duration = durationSeconds;
    }
}
