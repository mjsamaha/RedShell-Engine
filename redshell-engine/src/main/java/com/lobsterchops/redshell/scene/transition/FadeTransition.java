package com.lobsterchops.redshell.scene.transition;

import com.badlogic.gdx.graphics.Color;
import com.lobsterchops.redshell.rendering.Renderer2D;

/** Fades to a color, switches scenes at full opacity, then fades back in. */
public final class FadeTransition implements Transition {

    private final float duration;
    private final Color color;
    private float elapsed;

    public FadeTransition(float durationSeconds) {
        this(durationSeconds, Color.BLACK);
    }

    public FadeTransition(float durationSeconds, Color color) {
        if (durationSeconds <= 0f) {
            throw new IllegalArgumentException("duration must be positive: " + durationSeconds);
        }
        this.duration = durationSeconds;
        this.color = new Color(color);
    }

    @Override
    public void update(float deltaTime) {
        elapsed = Math.min(elapsed + deltaTime, duration);
    }

    @Override
    public boolean reachedMidpoint() {
        return elapsed >= duration / 2f;
    }

    @Override
    public boolean isFinished() {
        return elapsed >= duration;
    }

    /** Overlay opacity from 0 to 1 and back to 0. */
    public float alpha() {
        float half = duration / 2f;
        return elapsed < half ? elapsed / half : 1f - (elapsed - half) / half;
    }

    @Override
    public void render(Renderer2D renderer) {
        float alpha = alpha();
        if (alpha > 0f) {
            renderer.fillScreen(new Color(color.r, color.g, color.b, alpha));
        }
    }

    @Override
    public Transition copy() {
        return new FadeTransition(duration, color);
    }
}