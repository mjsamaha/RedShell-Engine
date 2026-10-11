package com.lobsterchops.redshell.rendering.sprite;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.lobsterchops.redshell.rendering.layer.RenderLayer;

/**
 * Lightweight, reusable draw descriptor. The sprite is drawn centered on ({@code x}, {@code y}).
 * Fields are public so render systems can refill pooled instances every frame without allocating.
 */
public final class Sprite2D {

    public TextureRegion region;
    public float x;
    public float y;
    public float width;
    public float height;
    public float scaleX = 1f;
    public float scaleY = 1f;
    public float rotationDegrees;
    public boolean flipX;
    public boolean flipY;
    public float red = 1f;
    public float green = 1f;
    public float blue = 1f;
    public float alpha = 1f;
    public RenderLayer layer = RenderLayer.ACTORS;
    public int zIndex;
    /** Value used to order sprites of the same layer and z-index: higher Y draws behind. */
    public float sortY;

    public void reset() {
        region = null;
        x = 0f;
        y = 0f;
        width = 0f;
        height = 0f;
        scaleX = 1f;
        scaleY = 1f;
        rotationDegrees = 0f;
        flipX = false;
        flipY = false;
        red = 1f;
        green = 1f;
        blue = 1f;
        alpha = 1f;
        layer = RenderLayer.ACTORS;
        zIndex = 0;
        sortY = 0f;
    }
}