package com.lobsterchops.redshell.rendering.animation;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.ArrayList;
import java.util.List;

/** An ordered list of frames with a frame duration, a loop mode and optional frame events. */
public final class Animation2D {

    private final String name;
    private final TextureRegion[] frames;
    private final float frameDuration;
    private final LoopMode loopMode;
    private final List<AnimationEvent> events = new ArrayList<>();

    public Animation2D(String name, TextureRegion[] frames, float frameDuration, LoopMode loopMode) {
        if (frames.length == 0) {
            throw new IllegalArgumentException("An animation needs at least one frame");
        }
        if (frameDuration <= 0f) {
            throw new IllegalArgumentException("frameDuration must be positive: " + frameDuration);
        }
        this.name = name;
        this.frames = new TextureRegion[frames.length];
        System.arraycopy(frames, 0, this.frames, 0, frames.length);
        this.frameDuration = frameDuration;
        this.loopMode = loopMode;
    }

    /** Registers an event to fire whenever playback enters the given frame. */
    public Animation2D addEvent(int frameIndex, String eventName) {
        if (frameIndex < 0 || frameIndex >= frames.length) {
            throw new IndexOutOfBoundsException("frame " + frameIndex + " of " + frames.length);
        }
        events.add(new AnimationEvent(name, frameIndex, eventName));
        return this;
    }

    public String name() {
        return name;
    }

    public int frameCount() {
        return frames.length;
    }

    public float frameDuration() {
        return frameDuration;
    }

    public LoopMode loopMode() {
        return loopMode;
    }

    public TextureRegion frame(int index) {
        return frames[index];
    }

    public List<AnimationEvent> events() {
        return events;
    }

    /** Which frame is shown at the given step (the number of frame durations elapsed). */
    public int frameIndexForStep(int step) {
        int count = frames.length;
        switch (loopMode) {
            case ONCE:
                return Math.min(step, count - 1);
            case LOOP:
                return step % count;
            default:
                if (count == 1) {
                    return 0;
                }
                int period = 2 * (count - 1);
                int phase = step % period;
                return phase < count ? phase : period - phase;
        }
    }

    public int stepAt(float time) {
        return (int) (time / frameDuration);
    }

    public int frameIndexAt(float time) {
        return frameIndexForStep(stepAt(time));
    }

    public TextureRegion frameAt(float time) {
        return frames[frameIndexAt(time)];
    }

    /** True for a {@link LoopMode#ONCE} animation whose last frame has been shown for its full duration. */
    public boolean isFinished(float time) {
        return loopMode == LoopMode.ONCE && stepAt(time) >= frames.length;
    }
}