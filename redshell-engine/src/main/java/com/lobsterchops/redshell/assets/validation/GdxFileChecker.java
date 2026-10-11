package com.lobsterchops.redshell.assets.validation;

import com.badlogic.gdx.Gdx;

/** Checks internal (asset folder) files through LibGDX. */
public final class GdxFileChecker implements FileChecker {

    @Override
    public boolean exists(String path) {
        return Gdx.files.internal(path).exists();
    }
}