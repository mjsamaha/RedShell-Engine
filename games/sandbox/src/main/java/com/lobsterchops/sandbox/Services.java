package com.lobsterchops.sandbox;

import com.lobsterchops.redshell.audio.AudioService;
import com.lobsterchops.redshell.input.InputService;
import com.lobsterchops.redshell.save.settings.SettingsService;
import com.lobsterchops.redshell.scene.SceneContext;
import com.lobsterchops.redshell.ui.UiService;

/** Looks up the engine services scenes use beyond what {@link SceneContext} offers. */
final class Services {

    private Services() {
    }

    static UiService ui(SceneContext context) {
        return context.engine().service(UiService.class);
    }

    static AudioService audio(SceneContext context) {
        return context.engine().hasService(AudioService.class) ? context.engine().service(AudioService.class) : null;
    }

    static SettingsService settings(SceneContext context) {
        return context.engine().service(SettingsService.class);
    }

    static InputService input(SceneContext context) {
        return context.input();
    }

    /** Plays a UI click if audio is available. */
    static void click(SceneContext context) {
        AudioService audio = audio(context);
        if (audio != null) {
            audio.playSound(SandboxSounds.CLICK);
        }
    }
}