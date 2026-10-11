package com.lobsterchops.redshell.core.time;

/**
 * Supplies per-frame time: scaled game delta, unscaled delta, totals, time scale and pause.
 * Pausing stops game time only; the engine keeps rendering.
 */
public final class GameClock {

    private final float maxFrameSeconds;
    private float timeScale = 1f;
    private boolean paused;
    private float deltaTime;
    private float unscaledDeltaTime;
    private float totalTime;
    private float unscaledTotalTime;
    private long frameCount;

    public GameClock(float maxFrameSeconds) {
        if (maxFrameSeconds <= 0f) {
            throw new IllegalArgumentException("maxFrameSeconds must be positive: " + maxFrameSeconds);
        }
        this.maxFrameSeconds = maxFrameSeconds;
    }

    /** Advances the clock by the real time since the previous frame. Long stalls are capped. */
    public void tick(float rawDeltaSeconds) {
        float raw = Math.min(Math.max(rawDeltaSeconds, 0f), maxFrameSeconds);
        unscaledDeltaTime = raw;
        deltaTime = paused ? 0f : raw * timeScale;
        unscaledTotalTime += raw;
        totalTime += deltaTime;
        frameCount++;
    }

    /** Game time that passed this frame (scaled, zero while paused). */
    public float deltaTime() {
        return deltaTime;
    }

    public float unscaledDeltaTime() {
        return unscaledDeltaTime;
    }

    /** Accumulated game time (scaled, excludes paused time). */
    public float totalTime() {
        return totalTime;
    }

    public float unscaledTotalTime() {
        return unscaledTotalTime;
    }

    public long frameCount() {
        return frameCount;
    }

    public float timeScale() {
        return timeScale;
    }

    public void setTimeScale(float timeScale) {
        if (timeScale < 0f) {
            throw new IllegalArgumentException("timeScale must not be negative: " + timeScale);
        }
        this.timeScale = timeScale;
    }

    public boolean isPaused() {
        return paused;
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        paused = false;
    }
}
