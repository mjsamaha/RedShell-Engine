package com.lobsterchops.redshell.input.context;

import com.lobsterchops.redshell.input.action.Action;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A named set of actions that is active while the context is on the {@link ContextStack}.
 * An opaque context disables every action it does not list, which is how a pause menu mutes gameplay.
 */
public final class InputContext {

    private final String name;
    private final boolean opaque;
    private final Set<Action> actions;

    public InputContext(String name, boolean opaque, Action... actions) {
        this.name = name;
        this.opaque = opaque;
        this.actions = new HashSet<>(Arrays.asList(actions));
    }

    public String name() {
        return name;
    }

    public boolean opaque() {
        return opaque;
    }

    public boolean contains(Action action) {
        return actions.contains(action);
    }
}