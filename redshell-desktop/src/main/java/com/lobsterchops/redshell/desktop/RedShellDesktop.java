package com.lobsterchops.redshell.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.lobsterchops.redshell.app.GameDefinition;
import com.lobsterchops.redshell.app.LaunchConfig;
import com.lobsterchops.redshell.app.RedShellApplication;

/** Generic desktop (LWJGL3) launcher: runs any {@link GameDefinition}. */
public final class RedShellDesktop {

    private RedShellDesktop() {
    }

    /** Call from {@code main}. Restarts the JVM first when the platform needs it (macOS, some Windows setups). */
    public static void launch(GameDefinition definition) {
        if (StartupHelper.startNewJvmIfRequired()) {
            return;
        }
        new Lwjgl3Application(new RedShellApplication(definition), configure(definition.launchConfig()));
    }

    static Lwjgl3ApplicationConfiguration configure(LaunchConfig launch) {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle(launch.title());
        configuration.useVsync(launch.vsync());
        // Zero means "match the monitor": the refresh rate plus 1 to cope with fractional rates.
        int foregroundFps = launch.targetFps() > 0
                ? launch.targetFps()
                : Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1;
        configuration.setForegroundFPS(foregroundFps);
        configuration.setResizable(launch.resizable());
        if (launch.fullscreen()) {
            configuration.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
        } else {
            configuration.setWindowedMode(launch.windowWidth(), launch.windowHeight());
        }
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        return configuration;
    }
}
