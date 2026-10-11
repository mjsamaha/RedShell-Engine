package com.lobsterchops.redshell.ui.menu;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.lobsterchops.redshell.ui.UiScreen;
import com.lobsterchops.redshell.ui.theme.UiTheme;
import java.util.ArrayList;
import java.util.List;

/** A centred column with a title and buttons: the building block for main, pause and dialog screens. */
public class MenuScreen extends UiScreen {

    private final String title;
    private final String message;
    private final boolean dim;
    private final List<String> labels = new ArrayList<>();
    private final List<Runnable> actions = new ArrayList<>();

    public MenuScreen(String title) {
        this(title, null, false);
    }

    /**
     * @param message optional line of text under the title (dialogs)
     * @param dim darken everything behind the menu (pause menus, dialogs)
     */
    public MenuScreen(String title, String message, boolean dim) {
        this.title = title;
        this.message = message;
        this.dim = dim;
    }

    public MenuScreen button(String label, Runnable action) {
        labels.add(label);
        actions.add(action);
        return this;
    }

    @Override
    protected void build(UiTheme theme) {
        Table root = root();
        if (dim) {
            root.setBackground(theme.solid(UiTheme.PANEL, 0f, 0f));
        }
        root.add(new Label(title, theme.titleStyle())).padBottom(message == null ? 16f : 8f).row();
        if (message != null) {
            root.add(new Label(message, theme.labelStyle())).padBottom(16f).row();
        }
        for (int i = 0; i < labels.size(); i++) {
            final Runnable action = actions.get(i);
            TextButton button = new TextButton(labels.get(i), theme.buttonStyle());
            button.pad(6f, 24f, 6f, 24f);
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    action.run();
                }
            });
            root.add(button).width(180f).padBottom(8f).row();
        }
    }
}