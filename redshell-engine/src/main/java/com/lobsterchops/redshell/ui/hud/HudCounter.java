package com.lobsterchops.redshell.ui.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.lobsterchops.redshell.ui.theme.UiTheme;

/** A "Coins: 3" style counter. */
public final class HudCounter implements HudElement {

    private final String prefix;
    private int value;
    private Label label;

    public HudCounter(String prefix, int initial) {
        this.prefix = prefix;
        this.value = initial;
    }

    public int value() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
        if (label != null) {
            label.setText(prefix + value);
        }
    }

    @Override
    public Actor build(UiTheme theme) {
        label = new Label(prefix + value, theme.labelStyle());
        return label;
    }
}