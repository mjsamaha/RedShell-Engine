package com.lobsterchops.redshell.assets.validation;

import com.lobsterchops.redshell.assets.catalog.AssetCatalog;
import com.lobsterchops.redshell.assets.catalog.AssetEntry;
import com.lobsterchops.redshell.assets.catalog.AssetType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Checks catalog entries before the game runs: the file exists, its extension suits the asset type (audio is limited
 * to formats every browser plays), and no file is registered under two IDs. Duplicate IDs and unknown group members
 * are already rejected by {@link AssetCatalog} when the catalog is built.
 */
public final class AssetValidator {

    private final FileChecker files;

    public AssetValidator(FileChecker files) {
        this.files = files;
    }

    /** Returns one message per problem; an empty list means the catalog is valid. */
    public List<String> validate(AssetCatalog catalog) {
        List<String> problems = new ArrayList<>();
        Map<String, String> idByPath = new HashMap<>();
        for (AssetEntry entry : catalog.entries()) {
            if (!files.exists(entry.path())) {
                problems.add("Missing file for asset '" + entry.id() + "': " + entry.path());
            }
            String[] allowed = allowedExtensions(entry.type());
            if (!hasExtension(entry.path(), allowed)) {
                problems.add("Unsupported file type for " + entry.type() + " asset '" + entry.id() + "': "
                        + entry.path() + " (expected " + join(allowed) + ")");
            }
            String other = idByPath.put(entry.path(), entry.id());
            if (other != null) {
                problems.add("Assets '" + other + "' and '" + entry.id() + "' use the same file: " + entry.path());
            }
        }
        return problems;
    }

    /** The extensions accepted for a type, lower case without the dot. */
    public static String[] allowedExtensions(AssetType type) {
        switch (type) {
            case TEXTURE:
            case PIXMAP:
                return new String[] {"png", "jpg", "jpeg"};
            case TEXTURE_ATLAS:
                return new String[] {"atlas"};
            case BITMAP_FONT:
                return new String[] {"fnt"};
            case SOUND:
            case MUSIC:
                return new String[] {"ogg", "mp3", "wav"};
            default:
                return new String[0];
        }
    }

    private static boolean hasExtension(String path, String[] allowed) {
        int dot = path.lastIndexOf('.');
        if (dot < 0) {
            return false;
        }
        String extension = path.substring(dot + 1).toLowerCase();
        for (int i = 0; i < allowed.length; i++) {
            if (allowed[i].equals(extension)) {
                return true;
            }
        }
        return false;
    }

    private static String join(String[] values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                builder.append('/');
            }
            builder.append(values[i]);
        }
        return builder.toString();
    }
}