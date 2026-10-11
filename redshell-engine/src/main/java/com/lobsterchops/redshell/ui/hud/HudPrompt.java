package com.lobsterchops.redshell.ui.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.lobsterchops.redshell.ui.theme.UiTheme;

/** A line of text that can be shown or hidden ("Press E to open"). */
public final class HudPrompt implements HudElement {

    private String text;
    private boolean visible = true;
    private Label label;

    public HudPrompt(String text) {
        this.text = text;
    }

    public void setText(String text) {
        this.text = text;
        if (label != null) {
            label.setText(text);
        }
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        if (label != null) {
            label.setVisible(visible);
        }
    }

    @Override
    public Actor build(UiTheme theme) {
        label = new Label(text, theme.labelStyle());
        label.setVisible(visible);
        return label;
    }
}