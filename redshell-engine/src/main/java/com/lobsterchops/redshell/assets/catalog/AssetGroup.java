package com.lobsterchops.redshell.assets.catalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A named bundle of asset IDs (for example MENU or LEVEL_1) that loads and unloads together. */
public final class AssetGroup {

    private final String name;
    private final List<String> ids;

    AssetGroup(String name, List<String> ids) {
        this.name = name;
        this.ids = Collections.unmodifiableList(new ArrayList<>(ids));
    }

    public String name() {
        return name;
    }

    public List<String> ids() {
        return ids;
    }
}