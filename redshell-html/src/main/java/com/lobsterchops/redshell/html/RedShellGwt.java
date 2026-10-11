package com.lobsterchops.redshell.html;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.lobsterchops.redshell.app.GameDefinition;
import com.lobsterchops.redshell.app.LaunchConfig;
import com.lobsterchops.redshell.app.RedShellApplication;

/**
 * Generic GWT (browser) launcher. Subclass it, return the game from {@link #createGameDefinition()}
 * and name the subclass as the module's entry point.
 */
public abstract class RedShellGwt extends GwtApplication {

    private GameDefinition definition;

    protected abstract GameDefinition createGameDefinition();

    private GameDefinition definition() {
        if (definition == null) {
            definition = createGameDefinition();
        }
        return definition;
    }

    @Override
    public GwtApplicationConfiguration getConfig() {
        LaunchConfig launch = definition().launchConfig();
        GwtApplicationConfiguration configuration = launch.resizable()
                ? new GwtApplicationConfiguration(true)
                : new GwtApplicationConfiguration(launch.windowWidth(), launch.windowHeight());
        configuration.padVertical = 0;
        configuration.padHorizontal = 0;
        return configuration;
    }

    @Override
    public ApplicationListener createApplicationListener() {
        return new RedShellApplication(definition());
    }
}
