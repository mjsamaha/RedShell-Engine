package com.lobsterchops.redshell.events.types;

import com.lobsterchops.redshell.events.Event;

/** Published after the active (top) scene changes. Names are null when there was no previous or no current scene. */
public final class SceneChangedEvent implements Event {

    private final String previousScene;
    private final String currentScene;

    public SceneChangedEvent(String previousScene, String currentScene) {
        this.previousScene = previousScene;
        this.currentScene = currentScene;
    }

    public String previousScene() {
        return previousScene;
    }

    public String currentScene() {
        return currentScene;
    }
}