package com.lobsterchops.sandbox;

import com.lobsterchops.redshell.assets.catalog.AssetGroup;
import com.lobsterchops.redshell.audio.AudioService;
import com.lobsterchops.redshell.input.context.InputContext;
import com.lobsterchops.redshell.rendering.Renderer2D;
import com.lobsterchops.redshell.scene.Scene;
import com.lobsterchops.redshell.scene.transition.FadeTransition;
import com.lobsterchops.redshell.ui.menu.MenuScreen;

/** Title screen. Play (or ENTER/SPACE) fades into the gameplay scene, loading its assets first. */
final class MenuScene extends Scene {

    private static final InputContext CONTEXT = new InputContext("menu", true, SandboxAction.CONFIRM);

    private final SandboxAssets assets;
    private MenuScreen screen;

    MenuScene(SandboxAssets assets) {
        this.assets = assets;
    }

    @Override
    public AssetGroup assets() {
        return assets.menu();
    }

    @Override
    public InputContext inputContext() {
        return CONTEXT;
    }

    @Override
    protected void onEnter() {
        Renderer2D renderer = context().renderer();
        if (renderer != null) {
            renderer.camera().setPosition(renderer.worldWidth() / 2f, renderer.worldHeight() / 2f);
            renderer.camera().setZoom(1f);
            renderer.camera().update();
        }
        AudioService audio = Services.audio(context());
        if (audio != null) {
            audio.playMusic(SandboxSounds.THEME, 1f);
        }
        screen = new MenuScreen("RedShell Sandbox")
                .button("Play", new Runnable() {
                    @Override
                    public void run() {
                        play();
                    }
                })
                .button("Settings", new Runnable() {
                    @Override
                    public void run() {
                        Services.click(context());
                        context().scenes().push(new SettingsScene());
                    }
                });
        Services.ui(context()).add(screen);
    }

    @Override
    protected void onUpdate(float deltaTime) {
        if (context().input().pressed(SandboxAction.CONFIRM)) {
            play();
        }
    }

    private void play() {
        Services.click(context());
        context().scenes().load(new GameplayScene(assets), new FadeTransition(0.4f));
    }

    @Override
    protected void onDispose() {
        Services.ui(context()).remove(screen);
    }
}