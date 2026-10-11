package com.lobsterchops.redshell.html;

import com.google.gwt.user.client.Window;
import com.lobsterchops.arena.ArenaGame;
import com.lobsterchops.redshell.app.GameDefinition;
import com.lobsterchops.sandbox.SandboxGame;

/** Starts a game in the browser: the sandbox by default, the arena with {@code index.html?game=arena}. */
public class GwtLauncher extends RedShellGwt {
    @Override
    protected GameDefinition createGameDefinition() {
        if ("arena".equals(Window.Location.getParameter("game"))) {
            return new ArenaGame();
        }
        return new SandboxGame();
    }
}