package com.lobsterchops.redshell.desktop;

import com.lobsterchops.sandbox.SandboxGame;

/** Starts the sandbox game on desktop. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        RedShellDesktop.launch(new SandboxGame());
    }
}
