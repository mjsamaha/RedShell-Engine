package com.lobsterchops.redshell.util.random;

import com.badlogic.gdx.math.RandomXS128;

import java.util.List;

/** Seedable random source with range, chance and weighted-pick helpers. Reproducible for a given seed. */
public final class SeededRandom {

    private final long seed;
    private final RandomXS128 random;

    public SeededRandom(long seed) {
        this.seed = seed;
        this.random = new RandomXS128(seed);
    }

    public long seed() {
        return seed;
    }

    /** Returns a value in [0, bound). */
    public int nextInt(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("bound must be positive: " + bound);
        }
        return random.nextInt(bound);
    }

    /** Returns a value in [min, max], both inclusive. */
    public int nextInt(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min must not exceed max: " + min + " > " + max);
        }
        return min + random.nextInt(max - min + 1);
    }

    /** Returns a value in [0, 1). */
    public float nextFloat() {
        return random.nextFloat();
    }

    /** Returns a value in [min, max). */
    public float nextFloat(float min, float max) {
        if (min > max) {
            throw new IllegalArgumentException("min must not exceed max: " + min + " > " + max);
        }
        return min + random.nextFloat() * (max - min);
    }

    public boolean nextBoolean() {
        return random.nextBoolean();
    }

    /** Returns true with the given probability between 0 and 1. */
    public boolean chance(float probability) {
        return random.nextFloat() < probability;
    }

    public <T> T pick(List<T> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("items must not be empty");
        }
        return items.get(random.nextInt(items.size()));
    }

    /** Picks an index with probability proportional to its weight. Weights must be non-negative and not all zero. */
    public int pickWeightedIndex(float[] weights) {
        float total = 0f;
        for (float weight : weights) {
            if (weight < 0f) {
                throw new IllegalArgumentException("weights must not be negative");
            }
            total += weight;
        }
        if (total <= 0f) {
            throw new IllegalArgumentException("at least one weight must be positive");
        }
        float roll = random.nextFloat() * total;
        float cumulative = 0f;
        int lastPositive = -1;
        for (int i = 0; i < weights.length; i++) {
            if (weights[i] > 0f) {
                lastPositive = i;
                cumulative += weights[i];
                if (roll < cumulative) {
                    return i;
                }
            }
        }
        return lastPositive;
    }

    public <T> T pickWeighted(List<T> items, float[] weights) {
        if (items.size() != weights.length) {
            throw new IllegalArgumentException("items and weights must have the same size");
        }
        return items.get(pickWeightedIndex(weights));
    }
}
