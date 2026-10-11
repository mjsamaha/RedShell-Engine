package com.lobsterchops.redshell.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.lobsterchops.redshell.assets.catalog.AssetCatalog;
import com.lobsterchops.redshell.assets.catalog.AssetEntry;
import com.lobsterchops.redshell.assets.catalog.AssetGroup;
import com.lobsterchops.redshell.assets.catalog.AssetType;
import com.lobsterchops.redshell.core.context.EngineContext;
import com.lobsterchops.redshell.core.lifecycle.EngineSystem;
import com.lobsterchops.redshell.debug.log.Log;
import java.util.HashMap;
import java.util.Map;

/** {@link AssetService} backed by LibGDX's {@link AssetManager}. It is the only class that loads or unloads assets. */
public final class GdxAssetService implements AssetService, EngineSystem {

    private static final String TAG = "Assets";

    private final AssetCatalog catalog;
    private final AssetManager manager;
    private final Map<String, Integer> references = new HashMap<>();
    private Log log;

    public GdxAssetService(AssetCatalog catalog) {
        this(catalog, new AssetManager());
    }

    public GdxAssetService(AssetCatalog catalog, AssetManager manager) {
        this.catalog = catalog;
        this.manager = manager;
    }

    @Override
    public void initialize(EngineContext context) {
        this.log = context.log();
    }

    @Override
    public AssetCatalog catalog() {
        return catalog;
    }

    @Override
    public void load(AssetGroup group) {
        for (String id : group.ids()) {
            AssetEntry entry = catalog.entry(id);
            manager.load(entry.path(), classFor(entry.type()));
            Integer count = references.get(id);
            references.put(id, count == null ? 1 : count + 1);
        }
    }

    @Override
    public void unload(AssetGroup group) {
        for (String id : group.ids()) {
            Integer count = references.get(id);
            if (count == null) {
                if (log != null) {
                    log.warn(TAG, "Unload of '" + id + "' ignored: not loaded");
                }
                continue;
            }
            manager.unload(catalog.entry(id).path());
            if (count == 1) {
                references.remove(id);
            } else {
                references.put(id, count - 1);
            }
        }
    }

    @Override
    public boolean update() {
        return manager.update();
    }

    @Override
    public float progress() {
        return manager.getProgress();
    }

    @Override
    public void finishLoading() {
        manager.finishLoading();
    }

    @Override
    public boolean isLoaded(String id) {
        AssetEntry entry = catalog.entry(id);
        return manager.isLoaded(entry.path(), classFor(entry.type()));
    }

    @Override
    public boolean isLoaded(AssetGroup group) {
        for (String id : group.ids()) {
            if (!isLoaded(id)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int referenceCount(String id) {
        Integer count = references.get(id);
        return count == null ? 0 : count;
    }

    @Override
    public <T> T get(String id, Class<T> type) {
        AssetEntry entry = catalog.entry(id);
        if (!manager.isLoaded(entry.path())) {
            throw new IllegalStateException("Asset '" + id + "' is not loaded; load its group first");
        }
        return manager.get(entry.path(), type);
    }

    @Override
    public void dispose() {
        manager.dispose();
        references.clear();
    }

    private static Class<?> classFor(AssetType type) {
        switch (type) {
            case TEXTURE:
                return Texture.class;
            case TEXTURE_ATLAS:
                return TextureAtlas.class;
            case BITMAP_FONT:
                return BitmapFont.class;
            case SOUND:
                return Sound.class;
            case MUSIC:
                return Music.class;
            case PIXMAP:
                return Pixmap.class;
            default:
                throw new IllegalArgumentException("Unsupported asset type: " + type);
        }
    }
}