package com.lobsterchops.redshell.ui.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.lobsterchops.redshell.ui.UiScreen;
import com.lobsterchops.redshell.ui.theme.UiTheme;
import java.util.ArrayList;
import java.util.List;

/**
 * An always-visible overlay stacked in the top-left corner. Add elements with {@link #counter}, {@link #bar} and
 * {@link #prompt}, then add the screen to the {@code UiService}.
 */
public final class HudScreen extends UiScreen {

    private final List<HudElement> elements = new ArrayList<>();

    public HudCounter counter(String prefix, int initial) {
        HudCounter counter = new HudCounter(prefix, initial);
        elements.add(counter);
        return counter;
    }

    public HudBar bar(float width) {
        HudBar bar = new HudBar(width);
        elements.add(bar);
        return bar;
    }

    public HudPrompt prompt(String text) {
        HudPrompt prompt = new HudPrompt(text);
        elements.add(prompt);
        return prompt;
    }

    @Override
    protected void build(UiTheme theme) {
        root().top().left().pad(8f);
        for (int i = 0; i < elements.size(); i++) {
            HudElement element = elements.get(i);
            Actor actor = element.build(theme);
            Cell<Actor> cell = root().add(actor).left().padBottom(4f);
            if (element instanceof HudBar) {
                cell.width(((HudBar) element).width());
            }
            cell.row();
        }
    }
}