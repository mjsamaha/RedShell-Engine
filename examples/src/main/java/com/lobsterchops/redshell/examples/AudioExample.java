package com.lobsterchops.redshell.examples;

import com.lobsterchops.redshell.assets.catalog.AssetGroup;
import com.lobsterchops.redshell.audio.AudioService;
import com.lobsterchops.redshell.scene.Scene;

/**
 * Audio: SPACE plays a sound effect, E starts or stops the music (with a fade), UP/DOWN change the master volume.
 * Sounds are referenced by catalog ID and must be in the scene's asset group; the mixer applies volume, categories
 * and mute in one place.
 */
final class AudioExample extends ExampleGame {

    @Override
    public String name() {
        return "Example: audio";
    }

    @Override
    public Scene createInitialScene() {
        return new AudioScene(group());
    }

    private static final class AudioScene extends Scene {
        private final AssetGroup group;
        private AudioService audio;

        AudioScene(AssetGroup group) {
            this.group = group;
        }

        @Override
        public AssetGroup assets() {
            return group;
        }

        @Override
        protected void onEnter() {
            context().renderer().camera().setPosition(320f, 180f);
            audio = context().engine().hasService(AudioService.class)
                    ? context().engine().service(AudioService.class)
                    : null;
        }

        @Override
        protected void onUpdate(float deltaTime) {
            if (audio == null) {
                return;
            }
            if (context().input().pressed(ExampleAction.PRIMARY)) {
                audio.playSound(SOUND_COIN);
            }
            if (context().input().pressed(ExampleAction.SECONDARY)) {
                if (audio.currentMusic() == null) {
                    audio.playMusic(SONG, 1f);
                } else {
                    audio.stopMusic(1f);
                }
            }
            if (context().input().pressed(ExampleAction.UP)) {
                changeVolume(0.1f);
            }
            if (context().input().pressed(ExampleAction.DOWN)) {
                changeVolume(-0.1f);
            }
        }

        private void changeVolume(float delta) {
            audio.mixer().setMasterVolume(audio.mixer().masterVolume() + delta);
            context().log().info("Audio", "master volume " + audio.mixer().masterVolume());
        }
    }
}