package com.lobsterchops.redshell.ui.menu;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.lobsterchops.redshell.audio.AudioCategory;
import com.lobsterchops.redshell.audio.AudioMixer;
import com.lobsterchops.redshell.input.InputService;
import com.lobsterchops.redshell.input.action.Action;
import com.lobsterchops.redshell.input.binding.Binding;
import com.lobsterchops.redshell.input.binding.BindingSet;
import com.lobsterchops.redshell.save.settings.SettingsService;
import com.lobsterchops.redshell.ui.UiScreen;
import com.lobsterchops.redshell.ui.theme.UiTheme;
import java.util.List;

/**
 * Volume sliders, mute, fullscreen and key rebinding. Every change is applied immediately and persisted
 * through the {@link SettingsService}.
 */
public final class SettingsScreen extends UiScreen {

    private final SettingsService settings;
    private final AudioMixer mixer;
    private final InputService input;
    private final Action[] rebindable;
    private final Runnable onBack;

    public SettingsScreen(SettingsService settings, AudioMixer mixer, InputService input, Action[] rebindable,
                          Runnable onBack) {
        this.settings = settings;
        this.mixer = mixer;
        this.input = input;
        this.rebindable = rebindable;
        this.onBack = onBack;
    }

    @Override
    protected void build(UiTheme theme) {
        Table root = root();
        root.setBackground(theme.solid(new Color(0.05f, 0.05f, 0.1f, 0.97f), 0f, 0f));
        root.add(new Label("Settings", theme.titleStyle())).colspan(2).padBottom(10f).row();

        addVolume(theme, "Master", -1);
        addVolume(theme, "Music", AudioCategory.MUSIC.ordinal());
        addVolume(theme, "Effects", AudioCategory.SFX.ordinal());

        final TextButton mute = new TextButton(muteText(), theme.buttonStyle());
        mute.pad(3f, 12f, 3f, 12f);
        mute.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mixer.setMuted(!mixer.muted());
                mute.setText(muteText());
                persistAudio();
            }
        });
        root.add(new Label("Sound", theme.labelStyle())).right().padRight(12f);
        root.add(mute).left().width(120f).padBottom(4f).row();

        final TextButton fullscreen = new TextButton(fullscreenText(), theme.buttonStyle());
        fullscreen.pad(3f, 12f, 3f, 12f);
        fullscreen.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settings.settings().fullscreen = !settings.settings().fullscreen;
                settings.applyDisplay(false);
                settings.save();
                fullscreen.setText(fullscreenText());
            }
        });
        root.add(new Label("Display", theme.labelStyle())).right().padRight(12f);
        root.add(fullscreen).left().width(120f).padBottom(10f).row();

        for (int i = 0; i < rebindable.length; i++) {
            addRebind(theme, rebindable[i]);
        }

        TextButton back = new TextButton("Back", theme.buttonStyle());
        back.pad(6f, 24f, 6f, 24f);
        back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onBack.run();
            }
        });
        root.add(back).colspan(2).width(180f).padTop(10f).row();
    }

    private void addVolume(UiTheme theme, String label, final int category) {
        final Slider slider = new Slider(0f, 1f, 0.05f, false, theme.sliderStyle());
        slider.setValue(category < 0 ? mixer.masterVolume() : mixer.categoryVolume(AudioCategory.values()[category]));
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (category < 0) {
                    mixer.setMasterVolume(slider.getValue());
                } else {
                    mixer.setCategoryVolume(AudioCategory.values()[category], slider.getValue());
                }
                if (!slider.isDragging()) {
                    persistAudio();
                }
            }
        });
        slider.addListener(new InputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                persistAudio();
            }
        });
        root().add(new Label(label, theme.labelStyle())).right().padRight(12f);
        root().add(slider).width(200f).left().padBottom(4f).row();
    }

    private void addRebind(final UiTheme theme, final Action action) {
        final TextButton button = new TextButton(bindingText(action), theme.buttonStyle());
        button.pad(3f, 12f, 3f, 12f);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                beginCapture(button, action);
            }
        });
        root().add(new Label(action.name(), theme.labelStyle())).right().padRight(12f);
        root().add(button).left().width(120f).padBottom(2f).row();
    }

    private void beginCapture(final TextButton button, final Action action) {
        final Stage stage = button.getStage();
        button.setText("press a key");
        stage.addCaptureListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                stage.removeCaptureListener(this);
                if (keycode != Input.Keys.ESCAPE) {
                    BindingSet bindings = input.bindings();
                    bindings.rebind(action, Binding.key(keycode));
                    settings.captureBindings(input);
                    settings.save();
                }
                button.setText(bindingText(action));
                event.stop();
                return true;
            }
        });
    }

    private String bindingText(Action action) {
        List<Binding> list = input.bindings().bindingsFor(action);
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                text.append(" / ");
            }
            String bindingText = list.get(i).toText();
            text.append(bindingText.startsWith("KEY:") ? bindingText.substring(4) : bindingText);
        }
        return text.length() == 0 ? "-" : text.toString();
    }

    private void persistAudio() {
        settings.captureFrom(mixer);
        settings.save();
    }

    private String muteText() {
        return mixer.muted() ? "Muted" : "On";
    }

    private String fullscreenText() {
        return settings.settings().fullscreen ? "Fullscreen" : "Windowed";
    }
}