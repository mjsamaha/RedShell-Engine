package com.lobsterchops.redshell.rendering.sprite;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.lobsterchops.redshell.assets.AssetService;

/** A texture cut into a regular grid of equally sized frames. Frames are addressed by column and row. */
public final class SpriteSheet {

    private final TextureRegion[][] frames;

    public SpriteSheet(Texture texture, int frameWidth, int frameHeight) {
        this.frames = TextureRegion.split(texture, frameWidth, frameHeight);
    }

    /** Builds a sheet from a texture asset that is already loaded. */
    public static SpriteSheet fromAsset(AssetService assets, String textureId, int frameWidth, int frameHeight) {
        return new SpriteSheet(assets.get(textureId, Texture.class), frameWidth, frameHeight);
    }

    public int columns() {
        return frames.length == 0 ? 0 : frames[0].length;
    }

    public int rows() {
        return frames.length;
    }

    public TextureRegion frame(int column, int row) {
        return frames[row][column];
    }

    /** The first {@code count} frames of a row, left to right. */
    public TextureRegion[] row(int row, int count) {
        TextureRegion[] result = new TextureRegion[count];
        for (int i = 0; i < count; i++) {
            result[i] = frames[row][i];
        }
        return result;
    }
}