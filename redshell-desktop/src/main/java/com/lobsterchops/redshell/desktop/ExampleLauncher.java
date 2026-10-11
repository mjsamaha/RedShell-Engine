package com.lobsterchops.redshell.desktop;

import com.lobsterchops.redshell.app.GameDefinition;
import com.lobsterchops.redshell.examples.Examples;

/** Runs one example by name: {@code gradlew :redshell-desktop:runExample -Pexample=ecs}. */
public final class ExampleLauncher {

    private ExampleLauncher() {
    }

    public static void main(String[] args) {
        GameDefinition game = args.length == 0 ? null : Examples.create(args[0]);
        if (game == null) {
            System.err.println("Usage: ExampleLauncher <example>. Examples: " + Examples.names());
            System.exit(1);
            return;
        }
        RedShellDesktop.launch(game);
    }
}