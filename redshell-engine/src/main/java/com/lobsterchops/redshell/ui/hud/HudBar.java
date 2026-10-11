package com.lobsterchops.redshell.ui.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.lobsterchops.redshell.ui.theme.UiTheme;

/** A horizontal bar showing a fraction from 0 to 1 (health, stamina, progress). */
public final class HudBar implements HudElement {

    private final float width;
    private float fraction = 1f;
    private ProgressBar bar;

    public HudBar(float width) {
        this.width = width;
    }

    public float fraction() {
        return fraction;
    }

    public void setFraction(float fraction) {
        this.fraction = fraction < 0f ? 0f : (fraction > 1f ? 1f : fraction);
        if (bar != null) {
            bar.setValue(this.fraction);
        }
    }

    @Override
    public Actor build(UiTheme theme) {
        bar = new ProgressBar(0f, 1f, 0.01f, false, theme.barStyle());
        bar.setValue(fraction);
        bar.setWidth(width);
        return bar;
    }

    float width() {
        return width;
    }
}