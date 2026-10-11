package com.lobsterchops.redshell.desktop;

import com.lobsterchops.redshell.app.GameDefinition;
import com.lobsterchops.redshell.assets.validation.AssetValidator;
import java.io.File;
import java.util.List;

/**
 * Checks game asset catalogs against the asset folder without starting a game.
 * Arguments: the asset folder, then one or more {@link GameDefinition} class names. Exits with 1 if anything is wrong.
 */
public final class ValidateAssets {

    private ValidateAssets() {
    }

    public static void main(String[] args) throws Exception {
        File root = new File(args[0]);
        int failures = 0;
        for (int i = 1; i < args.length; i++) {
            GameDefinition game = (GameDefinition) Class.forName(args[i]).getDeclaredConstructor().newInstance();
            List<String> problems = new AssetValidator(path -> new File(root, path).isFile())
                    .validate(game.assetCatalog());
            for (String problem : problems) {
                System.err.println("ERROR " + problem);
            }
            System.out.println(game.name() + ": " + game.assetCatalog().entries().size() + " assets, "
                    + problems.size() + " problem(s)");
            failures += problems.size();
        }
        if (failures > 0) {
            System.exit(1);
        }
    }
}