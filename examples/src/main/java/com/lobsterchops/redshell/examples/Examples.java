package com.lobsterchops.redshell.examples;

import com.lobsterchops.redshell.app.GameDefinition;
import java.util.Arrays;
import java.util.List;

/** Looks examples up by name. Run one with {@code gradlew :redshell-desktop:runExample -Pexample=ecs}. */
public final class Examples {

    private static final List<String> NAMES =
            Arrays.asList("sprite", "ecs", "scene-stack", "input-actions", "collision", "audio");

    private Examples() {
    }

    public static List<String> names() {
        return NAMES;
    }

    /** A fresh game definition for the named example, or null if the name is unknown. */
    public static GameDefinition create(String name) {
        switch (name) {
            case "sprite":
                return new SpriteExample();
            case "ecs":
                return new EcsExample();
            case "scene-stack":
                return new SceneStackExample();
            case "input-actions":
                return new InputActionsExample();
            case "collision":
                return new CollisionExample();
            case "audio":
                return new AudioExample();
            default:
                return null;
        }
    }
}