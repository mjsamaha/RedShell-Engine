package com.lobsterchops.arena;

import com.lobsterchops.redshell.assets.catalog.AssetGroup;
import com.lobsterchops.redshell.input.context.InputContext;
import com.lobsterchops.redshell.rendering.Renderer2D;
import com.lobsterchops.redshell.scene.Scene;
import com.lobsterchops.redshell.scene.transition.FadeTransition;
import com.lobsterchops.redshell.ui.menu.MenuScreen;

/** Title screen. */
final class MenuScene extends Scene {

    private static final InputContext CONTEXT = new InputContext("menu", true, ArenaAction.CONFIRM);

    private final ArenaAssets assets;
    private MenuScreen screen;

    MenuScene(ArenaAssets assets) {
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
        if (Services.audio(context()) != null) {
            Services.audio(context()).playMusic(ArenaSounds.THEME, 1f);
        }
        screen = new MenuScreen("RedShell Arena", "WASD move, arrow keys fire", false)
                .button("Play", new Runnable() {
                    @Override
                    public void run() {
                        play();
                    }
                });
        Services.ui(context()).add(screen);
    }

    @Override
    protected void onUpdate(float deltaTime) {
        if (context().input().pressed(ArenaAction.CONFIRM)) {
            play();
        }
    }

    private void play() {
        Services.play(context(), ArenaSounds.CLICK);
        context().scenes().load(new GameplayScene(assets), new FadeTransition(0.4f));
    }

    @Override
    protected void onDispose() {
        Services.ui(context()).remove(screen);
    }
}