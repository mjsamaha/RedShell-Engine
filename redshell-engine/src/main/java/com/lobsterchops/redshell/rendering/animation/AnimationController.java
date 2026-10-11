package com.lobsterchops.redshell.rendering.animation;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Switches between named animation states (idle, run, jump...) and reports frame events. */
public final class AnimationController {

    private static final int MAX_STEPS_PER_UPDATE = 1000;

    private final Map<String, Animation2D> states = new LinkedHashMap<>();
    private Animation2D current;
    private float time;
    private int lastStep = -1;
    private AnimationEventListener listener;

    public AnimationController addState(String stateName, Animation2D animation) {
        states.put(stateName, animation);
        return this;
    }

    public void setListener(AnimationEventListener listener) {
        this.listener = listener;
    }

    /** Switches to a state. Does nothing if it is already playing; use {@link #restart(String)} to force. */
    public void play(String stateName) {
        Animation2D next = states.get(stateName);
        if (next == null) {
            throw new IllegalArgumentException("Unknown animation state: " + stateName);
        }
        if (next != current) {
            start(next);
        }
    }

    public void restart(String stateName) {
        Animation2D next = states.get(stateName);
        if (next == null) {
            throw new IllegalArgumentException("Unknown animation state: " + stateName);
        }
        start(next);
    }

    private void start(Animation2D animation) {
        current = animation;
        time = 0f;
        lastStep = -1;
    }

    public void update(float deltaSeconds) {
        if (current == null || deltaSeconds <= 0f && lastStep >= 0) {
            return;
        }
        time += Math.max(deltaSeconds, 0f);
        int step = current.stepAt(time);
        int first = Math.max(lastStep + 1, step - MAX_STEPS_PER_UPDATE);
        for (int s = first; s <= step; s++) {
            if (current.loopMode() == LoopMode.ONCE && s >= current.frameCount()) {
                break;
            }
            fireEvents(current.frameIndexForStep(s));
        }
        lastStep = Math.max(lastStep, step);
    }

    private void fireEvents(int frame) {
        if (listener == null) {
            return;
        }
        List<AnimationEvent> events = current.events();
        for (int i = 0; i < events.size(); i++) {
            AnimationEvent event = events.get(i);
            if (event.frameIndex() == frame) {
                listener.onAnimationEvent(event);
            }
        }
    }

    /** The frame to draw now, or null when no state has been played. */
    public TextureRegion currentFrame() {
        return current == null ? null : current.frameAt(time);
    }

    public String currentState() {
        return current == null ? null : current.name();
    }

    public boolean isFinished() {
        return current != null && current.isFinished(time);
    }
}