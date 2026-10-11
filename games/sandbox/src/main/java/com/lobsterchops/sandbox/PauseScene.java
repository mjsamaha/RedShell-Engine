package com.lobsterchops.sandbox;

import com.lobsterchops.redshell.input.context.InputContext;
import com.lobsterchops.redshell.scene.Scene;
import com.lobsterchops.redshell.scene.SceneFlags;
import com.lobsterchops.redshell.scene.transition.FadeTransition;
import com.lobsterchops.redshell.ui.menu.MenuScreen;

/** Overlay on top of gameplay. Gameplay is drawn but frozen; its actions are disabled by this context. */
final class PauseScene extends Scene {

    private static final InputContext CONTEXT = new InputContext("pause", true,
            SandboxAction.PAUSE, SandboxAction.CONFIRM, SandboxAction.BACK);

    private final SandboxAssets assets;
    private MenuScreen screen;

    PauseScene(SandboxAssets assets) {
        this.assets = assets;
    }

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
        screen = new MenuScreen("Paused", null, true)
                .button("Resume", new Runnable() {
                    @Override
                    public void run() {
                        resumeGame();
                    }
                })
                .button("Settings", new Runnable() {
                    @Override
                    public void run() {
                        Services.click(context());
                        context().scenes().push(new SettingsScene());
                    }
                })
                .button("Main menu", new Runnable() {
                    @Override
                    public void run() {
                        mainMenu();
                    }
                });
        Services.ui(context()).add(screen);
    }

    @Override
    protected void onUpdate(float deltaTime) {
        if (context().input().pressed(SandboxAction.PAUSE) || context().input().pressed(SandboxAction.CONFIRM)) {
            resumeGame();
        } else if (context().input().pressed(SandboxAction.BACK)) {
            mainMenu();
        }
    }

    private void resumeGame() {
        Services.click(context());
        context().scenes().pop();
    }

    private void mainMenu() {
        Services.click(context());
        context().scenes().load(new MenuScene(assets), new FadeTransition(0.4f));
    }

    @Override
    protected void onDispose() {
        Services.ui(context()).remove(screen);
    }
}