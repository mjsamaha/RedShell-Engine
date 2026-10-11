package com.lobsterchops.sandbox;

import com.lobsterchops.redshell.audio.AudioService;
import com.lobsterchops.redshell.input.action.Action;
import com.lobsterchops.redshell.input.context.InputContext;
import com.lobsterchops.redshell.audio.AudioMixer;
import com.lobsterchops.redshell.scene.Scene;
import com.lobsterchops.redshell.scene.SceneFlags;
import com.lobsterchops.redshell.ui.menu.SettingsScreen;

/** Volume, mute, display and key rebinding. Pushed over the menu or the pause menu. */
final class SettingsScene extends Scene {

    // No actions: while a key is being captured for rebinding, nothing else should react to it.
    private static final InputContext CONTEXT = new InputContext("settings", true);

    private SettingsScreen screen;

    @Override
    public SceneFlags flags() {
        return SceneFlags.OVERLAY;
    }

    @Override
    public InputContext inputContext() {
        return CONTEXT;
    }

    @Override
    protected void onEnter() {
        AudioService audio = Services.audio(context());
        AudioMixer mixer = audio != null ? audio.mixer() : new AudioMixer();
        Action[] rebindable = {SandboxAction.MOVE_UP, SandboxAction.MOVE_DOWN, SandboxAction.MOVE_LEFT,
                SandboxAction.MOVE_RIGHT, SandboxAction.PAUSE};
        screen = new SettingsScreen(Services.settings(context()), mixer, context().input(), rebindable, new Runnable() {
            @Override
            public void run() {
                Services.click(context());
                context().scenes().pop();
            }
        });
        Services.ui(context()).add(screen);
    }

    @Override
    protected void onDispose() {
        Services.ui(context()).remove(screen);
    }
}