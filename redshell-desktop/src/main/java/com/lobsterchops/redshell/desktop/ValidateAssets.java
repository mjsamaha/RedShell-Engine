package com.lobsterchops.redshell.desktop;

import com.lobsterchops.redshell.app.GameDefinition;
import com.lobsterchops.redshell.assets.validation.AssetValidator;
import java.io.File;
import java.util.List;

/**
 * Checks a game's asset catalog against the asset folder without starting the game.
 * Arguments: the asset folder, then the {@link GameDefinition} class name. Exits with 1 if anything is wrong.
 */
public final class ValidateAssets {

    private ValidateAssets() {
    }

    public static void main(String[] args) throws Exception {
        File root = new File(args.length > 0 ? args[0] : "assets");
        String gameClass = args.length > 1 ? args[1] : "com.lobsterchops.sandbox.SandboxGame";
        GameDefinition game = (GameDefinition) Class.forName(gameClass).getDeclaredConstructor().newInstance();
        List<String> problems = new AssetValidator(path -> new File(root, path).isFile()).validate(game.assetCatalog());
        for (String problem : problems) {
            System.err.println("ERROR " + problem);
        }
        System.out.println(game.name() + ": " + game.assetCatalog().entries().size() + " assets, "
                + problems.size() + " problem(s)");
        if (!problems.isEmpty()) {
            System.exit(1);
        }
    }
}