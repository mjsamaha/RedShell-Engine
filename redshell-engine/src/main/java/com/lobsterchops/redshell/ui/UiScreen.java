package com.lobsterchops.redshell.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.lobsterchops.redshell.ui.theme.UiTheme;

/**
 * One layer of UI (a HUD, a menu, a dialog). Subclasses fill {@link #root()} in {@link #build}. Add it with
 * {@link UiService#add} when a scene enters and remove it when the scene is disposed.
 */
public abstract class UiScreen {

    private final Table root = new Table();
    private Stage stage;

    /** The full-screen table every widget of this screen lives in. */
    public final Table root() {
        return root;
    }

    /** The stage this screen is attached to, or null. */
    public final Stage stage() {
        return stage;
    }

    protected abstract void build(UiTheme theme);

    final void attach(Stage target, UiTheme theme) {
        this.stage = target;
        root.setFillParent(true);
        build(theme);
        target.addActor(root);
    }

    final void detach() {
        root.remove();
        stage = null;
    }
}