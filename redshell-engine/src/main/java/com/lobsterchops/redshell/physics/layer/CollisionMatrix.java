package com.lobsterchops.redshell.physics.layer;

/** Which layer pairs may collide. Symmetric. A new matrix lets every layer collide with every layer. */
public final class CollisionMatrix {

    private final int[] masks = new int[CollisionLayer.MAX_LAYERS];

    public CollisionMatrix() {
        for (int i = 0; i < masks.length; i++) {
            masks[i] = -1;
        }
    }

    public CollisionMatrix setCollides(int layerA, int layerB, boolean collides) {
        CollisionLayer.check(layerA);
        CollisionLayer.check(layerB);
        if (collides) {
            masks[layerA] |= 1 << layerB;
            masks[layerB] |= 1 << layerA;
        } else {
            masks[layerA] &= ~(1 << layerB);
            masks[layerB] &= ~(1 << layerA);
        }
        return this;
    }

    public boolean collides(int layerA, int layerB) {
        return (masks[layerA] & (1 << layerB)) != 0;
    }

    /** Bit mask of layers the given layer collides with. */
    public int maskFor(int layer) {
        return masks[layer];
    }
}