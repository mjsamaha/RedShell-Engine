package com.lobsterchops.arena;

import com.lobsterchops.redshell.audio.AudioService;
import com.lobsterchops.redshell.scene.SceneContext;
import com.lobsterchops.redshell.ui.UiService;

/** Looks up the engine services scenes use beyond what the scene context offers. */
final class Services {

    private Services() {
    }

    static UiService ui(SceneContext context) {
        return context.engine().service(UiService.class);
    }

    static AudioService audio(SceneContext context) {
        return context.engine().hasService(AudioService.class) ? context.engine().service(AudioService.class) : null;
    }

    static void play(SceneContext context, String soundId) {
        AudioService audio = audio(context);
        if (audio != null) {
            audio.playSound(soundId);
        }
    }
}