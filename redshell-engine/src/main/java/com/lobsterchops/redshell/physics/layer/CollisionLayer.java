package com.lobsterchops.redshell.physics.layer;

/** Layer indices (0 to 31) and the bit helpers that turn them into masks. */
public final class CollisionLayer {

    public static final int MAX_LAYERS = 32;

    private CollisionLayer() {
    }

    public static int bit(int layer) {
        check(layer);
        return 1 << layer;
    }

    public static int mask(int... layers) {
        int mask = 0;
        for (int layer : layers) {
            mask |= bit(layer);
        }
        return mask;
    }

    static void check(int layer) {
        if (layer < 0 || layer >= MAX_LAYERS) {
            throw new IllegalArgumentException("Collision layer must be 0-31: " + layer);
        }
    }
}