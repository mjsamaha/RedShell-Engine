package com.lobsterchops.arena;

import com.lobsterchops.redshell.assets.catalog.AssetGroup;
import com.lobsterchops.redshell.input.context.InputContext;
import com.lobsterchops.redshell.scene.Scene;
import com.lobsterchops.redshell.scene.transition.FadeTransition;
import com.lobsterchops.redshell.ui.menu.MenuScreen;

/** Shown when the last life is lost. */
final class GameOverScene extends Scene {

    private static final InputContext CONTEXT = new InputContext("gameover", true, ArenaAction.CONFIRM);

    private final ArenaAssets assets;
    private final int score;
    private MenuScreen screen;

    GameOverScene(ArenaAssets assets, int score) {
        this.assets = assets;
        this.score = score;
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
        screen = new MenuScreen("Game over", "Enemies destroyed: " + score, false)
                .button("Play again", new Runnable() {
                    @Override
                    public void run() {
                        again();
                    }
                })
                .button("Main menu", new Runnable() {
                    @Override
                    public void run() {
                        Services.play(context(), ArenaSounds.CLICK);
                        context().scenes().load(new MenuScene(assets), new FadeTransition(0.4f));
                    }
                });
        Services.ui(context()).add(screen);
    }

    @Override
    protected void onUpdate(float deltaTime) {
        if (context().input().pressed(ArenaAction.CONFIRM)) {
            again();
        }
    }

    private void again() {
        Services.play(context(), ArenaSounds.CLICK);
        context().scenes().load(new GameplayScene(assets), new FadeTransition(0.4f));
    }

    @Override
    protected void onDispose() {
        Services.ui(context()).remove(screen);
    }
}