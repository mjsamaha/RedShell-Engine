package com.lobsterchops.redshell.util.math;

/** Clamp, lerp, easing, angle and small geometry helpers. All angles are in degrees. */
public final class MathUtil {

    private MathUtil() {
    }

    public static float clamp(float value, float min, float max) {
        if (min > max) {
            throw new IllegalArgumentException("min must not exceed max: " + min + " > " + max);
        }
        return value < min ? min : (value > max ? max : value);
    }

    public static int clamp(int value, int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min must not exceed max: " + min + " > " + max);
        }
        return value < min ? min : (value > max ? max : value);
    }

    public static float clamp01(float value) {
        return clamp(value, 0f, 1f);
    }

    public static float lerp(float from, float to, float t) {
        return from + (to - from) * t;
    }

    /** Returns where {@code value} lies between {@code from} and {@code to}, or 0 when the range is empty. */
    public static float inverseLerp(float from, float to, float value) {
        if (from == to) {
            return 0f;
        }
        return (value - from) / (to - from);
    }

    public static float remap(float value, float fromMin, float fromMax, float toMin, float toMax) {
        return lerp(toMin, toMax, inverseLerp(fromMin, fromMax, value));
    }

    public static boolean approximately(float a, float b, float epsilon) {
        return Math.abs(a - b) <= epsilon;
    }

    public static float smoothStep(float t) {
        float c = clamp01(t);
        return c * c * (3f - 2f * c);
    }

    public static float easeInQuad(float t) {
        float c = clamp01(t);
        return c * c;
    }

    public static float easeOutQuad(float t) {
        float c = clamp01(t);
        return 1f - (1f - c) * (1f - c);
    }

    public static float easeInOutQuad(float t) {
        float c = clamp01(t);
        return c < 0.5f ? 2f * c * c : 1f - 2f * (1f - c) * (1f - c);
    }

    public static float easeInCubic(float t) {
        float c = clamp01(t);
        return c * c * c;
    }

    public static float easeOutCubic(float t) {
        float inverse = 1f - clamp01(t);
        return 1f - inverse * inverse * inverse;
    }

    public static float easeInOutCubic(float t) {
        float c = clamp01(t);
        if (c < 0.5f) {
            return 4f * c * c * c;
        }
        float f = -2f * c + 2f;
        return 1f - f * f * f / 2f;
    }

    /** Wraps an angle into the range [0, 360). */
    public static float normalizeDegrees(float degrees) {
        float result = degrees % 360f;
        return result < 0f ? result + 360f : result;
    }

    /** Smallest signed rotation from {@code from} to {@code to}, in the range [-180, 180). */
    public static float shortestAngleDelta(float from, float to) {
        float delta = normalizeDegrees(to - from);
        return delta >= 180f ? delta - 360f : delta;
    }

    public static float lerpAngle(float from, float to, float t) {
        return normalizeDegrees(from + shortestAngleDelta(from, to) * t);
    }

    public static float distanceSquared(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return dx * dx + dy * dy;
    }

    public static float distance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(distanceSquared(x1, y1, x2, y2));
    }
}
