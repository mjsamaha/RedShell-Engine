package com.lobsterchops.redshell.html;

import com.lobsterchops.redshell.app.GameDefinition;
import com.lobsterchops.sandbox.SandboxGame;

/** Starts the sandbox game in the browser. */
public class GwtLauncher extends RedShellGwt {
    @Override
    protected GameDefinition createGameDefinition() {
        return new SandboxGame();
    }
}
