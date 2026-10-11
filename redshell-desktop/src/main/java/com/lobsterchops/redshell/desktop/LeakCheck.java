package com.lobsterchops.redshell.desktop;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Texture;
import com.lobsterchops.redshell.app.GameDefinition;
import com.lobsterchops.redshell.assets.GdxAssetService;
import com.lobsterchops.redshell.assets.catalog.AssetGroup;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads and unloads every asset group of a game many times, including overlapping loads, and fails if any asset or
 * GL texture is still alive afterwards. Needs a GL context, so it is run by hand: {@code gradlew
 * :redshell-desktop:leakCheck}. Arguments: a {@link GameDefinition} class name and an optional cycle count.
 */
public final class LeakCheck {

    private static final int DEFAULT_CYCLES = 25;

    private LeakCheck() {
    }

    public static void main(String[] args) throws Exception {
        final GameDefinition game = (GameDefinition) Class.forName(args[0]).getDeclaredConstructor().newInstance();
        final int cycles = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_CYCLES;
        final int[] result = {1};
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("RedShell leak check");
        configuration.setWindowedMode(64, 64);
        configuration.setInitialVisible(false);
        new Lwjgl3Application(new ApplicationAdapter() {
            @Override
            public void create() {
                result[0] = check(game, cycles);
                Gdx.app.exit();
            }
        }, configuration);
        System.exit(result[0]);
    }

    private static int check(GameDefinition game, int cycles) {
        AssetManager manager = new AssetManager();
        GdxAssetService assets = new GdxAssetService(game.assetCatalog(), manager);
        List<AssetGroup> groups = new ArrayList<>(game.assetCatalog().groups());
        int peakTextures = 0;
        for (int cycle = 0; cycle < cycles; cycle++) {
            for (int i = 0; i < groups.size(); i++) {
                assets.load(groups.get(i));
                assets.finishLoading();
                peakTextures = Math.max(peakTextures, Texture.getNumManagedTextures());
                assets.unload(groups.get(i));
            }
            for (int i = 0; i < groups.size(); i++) {
                assets.load(groups.get(i));
            }
            assets.finishLoading();
            for (int i = 0; i < groups.size(); i++) {
                assets.unload(groups.get(i));
            }
            manager.update();
        }
        int loaded = manager.getLoadedAssets();
        int textures = Texture.getNumManagedTextures();
        System.out.println(game.name() + ": " + cycles + " cycles x " + groups.size() + " groups, peak textures "
                + peakTextures + ", assets left " + loaded + ", textures left " + textures);
        assets.dispose();
        manager.dispose();
        if (loaded != 0 || textures != 0) {
            System.err.println("LEAK: assets or textures were not released");
            return 1;
        }
        return 0;
    }
}