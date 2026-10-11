package com.lobsterchops.redshell.desktop;

import com.lobsterchops.arena.ArenaGame;
import com.lobsterchops.redshell.app.GameDefinition;
import com.lobsterchops.sandbox.SandboxGame;

/** Starts a game on desktop: {@code sandbox} (the default) or {@code arena}. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        String name = args.length == 0 ? "sandbox" : args[0];
        GameDefinition game;
        if ("arena".equals(name)) {
            game = new ArenaGame();
        } else if ("sandbox".equals(name)) {
            game = new SandboxGame();
        } else {
            System.err.println("Unknown game '" + name + "'. Use sandbox or arena.");
            System.exit(1);
            return;
        }
        RedShellDesktop.launch(game);
    }
}