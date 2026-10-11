package com.lobsterchops.redshell.debug.profiler;

/** Rolling statistics over the most recent samples (for example frame times in milliseconds). */
public final class FrameStats {

    private final float[] samples;
    private int next;
    private int count;
    private float sum;

    public FrameStats(int window) {
        if (window <= 0) {
            throw new IllegalArgumentException("window must be positive: " + window);
        }
        this.samples = new float[window];
    }

    public void record(float value) {
        if (count == samples.length) {
            sum -= samples[next];
        } else {
            count++;
        }
        samples[next] = value;
        sum += value;
        next = (next + 1) % samples.length;
    }

    public int count() {
        return count;
    }

    public float latest() {
        return count == 0 ? 0f : samples[(next - 1 + samples.length) % samples.length];
    }

    public float average() {
        return count == 0 ? 0f : sum / count;
    }

    public float max() {
        float max = 0f;
        for (int i = 0; i < count; i++) {
            max = Math.max(max, samples[i]);
        }
        return max;
    }

    public float min() {
        if (count == 0) {
            return 0f;
        }
        float min = Float.MAX_VALUE;
        for (int i = 0; i < count; i++) {
            min = Math.min(min, samples[i]);
        }
        return min;
    }

    public void reset() {
        next = 0;
        count = 0;
        sum = 0f;
    }
}