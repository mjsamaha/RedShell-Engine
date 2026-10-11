package com.lobsterchops.redshell.entity.component;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.lobsterchops.redshell.rendering.layer.RenderLayer;

/** What to draw for an entity: a region, its size, tint, layer and flip. Drawn centered on the transform. */
public final class SpriteComponent implements Component {

    public TextureRegion region;
    public float width;
    public float height;
    public float red = 1f;
    public float green = 1f;
    public float blue = 1f;
    public float alpha = 1f;
    public RenderLayer layer = RenderLayer.ACTORS;
    public int zIndex;
    public boolean flipX;
    public boolean flipY;
    public boolean visible = true;

    public SpriteComponent() {
    }

    public SpriteComponent(TextureRegion region, float width, float height, RenderLayer layer) {
        this.region = region;
        this.width = width;
        this.height = height;
        this.layer = layer;
    }
}