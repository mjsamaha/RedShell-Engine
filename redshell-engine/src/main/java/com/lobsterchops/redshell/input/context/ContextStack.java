package com.lobsterchops.redshell.input.context;

import com.lobsterchops.redshell.input.action.Action;
import java.util.ArrayList;
import java.util.List;

/** Stack of input contexts. An action is active if the first context (from the top) that lists it comes before any opaque one. */
public final class ContextStack {

    private final List<InputContext> contexts = new ArrayList<>();

    public void push(InputContext context) {
        contexts.add(context);
    }

    public InputContext pop() {
        if (contexts.isEmpty()) {
            throw new IllegalStateException("No input context to pop");
        }
        return contexts.remove(contexts.size() - 1);
    }

    /** Removes the topmost occurrence of the context, wherever it is. Returns false if it was not on the stack. */
    public boolean remove(InputContext context) {
        int index = contexts.lastIndexOf(context);
        if (index < 0) {
            return false;
        }
        contexts.remove(index);
        return true;
    }

    public InputContext peek() {
        return contexts.isEmpty() ? null : contexts.get(contexts.size() - 1);
    }

    public int size() {
        return contexts.size();
    }

    /** With no context on the stack nothing is restricted; otherwise an action must be listed by a reachable context. */
    public boolean isActive(Action action) {
        if (contexts.isEmpty()) {
            return true;
        }
        for (int i = contexts.size() - 1; i >= 0; i--) {
            InputContext context = contexts.get(i);
            if (context.contains(action)) {
                return true;
            }
            if (context.opaque()) {
                return false;
            }
        }
        return false;
    }
}