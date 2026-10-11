package com.lobsterchops.redshell.rendering.sprite;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.lobsterchops.redshell.assets.AssetService;

/** A region of a catalog atlas, named by atlas asset ID and region name. Resolved lazily and cached. */
public final class AtlasRegionRef {

    private final String atlasId;
    private final String regionName;
    private TextureRegion cached;

    public AtlasRegionRef(String atlasId, String regionName) {
        this.atlasId = atlasId;
        this.regionName = regionName;
    }

    public String atlasId() {
        return atlasId;
    }

    public String regionName() {
        return regionName;
    }

    public TextureRegion resolve(AssetService assets) {
        if (cached == null) {
            cached = assets.get(atlasId, TextureAtlas.class).findRegion(regionName);
            if (cached == null) {
                throw new IllegalStateException("Region '" + regionName + "' not found in atlas '" + atlasId + "'");
            }
        }
        return cached;
    }
}