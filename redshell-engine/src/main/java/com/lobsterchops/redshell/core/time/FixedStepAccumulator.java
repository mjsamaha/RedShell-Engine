package com.lobsterchops.redshell.core.time;

/** Converts variable frame time into a whole number of fixed simulation steps, capping time after stalls. */
public final class FixedStepAccumulator {

    private final float stepSeconds;
    private final float maxAccumulatedSeconds;
    private float accumulated;

    public FixedStepAccumulator(float stepSeconds, float maxAccumulatedSeconds) {
        if (stepSeconds <= 0f) {
            throw new IllegalArgumentException("stepSeconds must be positive: " + stepSeconds);
        }
        if (maxAccumulatedSeconds < stepSeconds) {
            throw new IllegalArgumentException("maxAccumulatedSeconds must be at least stepSeconds");
        }
        this.stepSeconds = stepSeconds;
        this.maxAccumulatedSeconds = maxAccumulatedSeconds;
    }

    /** Adds frame time and returns how many fixed steps should run now. Excess time beyond the cap is dropped. */
    public int advance(float deltaSeconds) {
        accumulated = Math.min(accumulated + Math.max(deltaSeconds, 0f), maxAccumulatedSeconds);
        int steps = (int) (accumulated / stepSeconds);
        accumulated -= steps * stepSeconds;
        return steps;
    }

    public float stepSeconds() {
        return stepSeconds;
    }

    /** Fraction of a step left over, between 0 and 1, for render interpolation. */
    public float alpha() {
        return accumulated / stepSeconds;
    }

    public void reset() {
        accumulated = 0f;
    }
}
