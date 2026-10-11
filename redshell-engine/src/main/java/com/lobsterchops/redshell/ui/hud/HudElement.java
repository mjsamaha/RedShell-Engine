package com.lobsterchops.redshell.ui.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.lobsterchops.redshell.ui.theme.UiTheme;

/** A widget of a {@link HudScreen}. Its state can be set before the widget exists; it is applied when built. */
public interface HudElement {

    /** Creates the widget and applies the stored state. */
    Actor build(UiTheme theme);
}