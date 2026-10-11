package com.lobsterchops.redshell.assets.validation;

import com.lobsterchops.redshell.assets.catalog.AssetCatalog;
import com.lobsterchops.redshell.assets.catalog.AssetEntry;
import java.util.ArrayList;
import java.util.List;

/** Checks catalog entries against the file system before the game runs. */
public final class AssetValidator {

    private final FileChecker files;

    public AssetValidator(FileChecker files) {
        this.files = files;
    }

    /** Returns one message per problem; an empty list means the catalog is valid. */
    public List<String> validate(AssetCatalog catalog) {
        List<String> problems = new ArrayList<>();
        for (AssetEntry entry : catalog.entries()) {
            if (!files.exists(entry.path())) {
                problems.add("Missing file for asset '" + entry.id() + "': " + entry.path());
            }
        }
        return problems;
    }
}