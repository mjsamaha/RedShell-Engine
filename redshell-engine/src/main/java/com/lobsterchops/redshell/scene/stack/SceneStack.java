package com.lobsterchops.redshell.scene.stack;

import com.lobsterchops.redshell.scene.Scene;
import com.lobsterchops.redshell.scene.SceneContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The ordered stack of active scenes. The top scene's flags decide whether the scenes below keep
 * updating and/or rendering. Rendering is bottom to top.
 */
public final class SceneStack {

    private final SceneContext context;
    private final List<Scene> scenes = new ArrayList<>();

    public SceneStack(SceneContext context) {
        this.context = context;
    }

    public void push(Scene scene) {
        scene.enter(context);
        scenes.add(scene);
    }

    public Scene pop() {
        if (scenes.isEmpty()) {
            throw new IllegalStateException("The scene stack is empty");
        }
        Scene scene = scenes.remove(scenes.size() - 1);
        scene.exit();
        return scene;
    }

    /** Exits the top scene, then enters the new one in its place. */
    public void replace(Scene scene) {
        if (!scenes.isEmpty()) {
            pop();
        }
        push(scene);
    }

    /** Exits every scene, top first. */
    public void clear() {
        while (!scenes.isEmpty()) {
            pop();
        }
    }

    public Scene top() {
        return scenes.isEmpty() ? null : scenes.get(scenes.size() - 1);
    }

    public int size() {
        return scenes.size();
    }

    public List<Scene> scenes() {
        return Collections.unmodifiableList(scenes);
    }

    public void update(float deltaTime) {
        int first = scenes.size() - 1;
        while (first > 0 && scenes.get(first).flags().updateBelow()) {
            first--;
        }
        for (int i = Math.max(first, 0); i < scenes.size(); i++) {
            scenes.get(i).update(deltaTime);
        }
    }

    public void render(float deltaTime) {
        int first = scenes.size() - 1;
        while (first > 0 && scenes.get(first).flags().renderBelow()) {
            first--;
        }
        for (int i = Math.max(first, 0); i < scenes.size(); i++) {
            scenes.get(i).render(deltaTime);
        }
    }

    public void resize(int width, int height) {
        for (int i = 0; i < scenes.size(); i++) {
            scenes.get(i).resize(width, height);
        }
    }

    public void pause() {
        for (int i = 0; i < scenes.size(); i++) {
            scenes.get(i).pause();
        }
    }

    public void resume() {
        for (int i = 0; i < scenes.size(); i++) {
            scenes.get(i).resume();
        }
    }
}