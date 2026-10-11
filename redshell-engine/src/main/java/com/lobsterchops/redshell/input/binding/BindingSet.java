package com.lobsterchops.redshell.input.binding;

import com.lobsterchops.redshell.input.action.Action;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** A full binding profile: which physical inputs trigger each action. Can be rebound and saved. */
public final class BindingSet {

    private final Map<Action, List<Binding>> bindings = new LinkedHashMap<>();

    /** Adds bindings to an action, keeping any it already has. */
    public BindingSet bind(Action action, Binding... added) {
        List<Binding> list = bindings.get(action);
        if (list == null) {
            list = new ArrayList<>();
            bindings.put(action, list);
        }
        for (Binding binding : added) {
            if (!list.contains(binding)) {
                list.add(binding);
            }
        }
        return this;
    }

    /** Replaces all bindings of an action (rebinding). */
    public BindingSet rebind(Action action, Binding... replacement) {
        bindings.remove(action);
        return bind(action, replacement);
    }

    public BindingSet unbind(Action action) {
        bindings.remove(action);
        return this;
    }

    public List<Binding> bindingsFor(Action action) {
        List<Binding> list = bindings.get(action);
        return list == null ? Collections.<Binding>emptyList() : Collections.unmodifiableList(list);
    }

    public Set<Action> actions() {
        return Collections.unmodifiableSet(bindings.keySet());
    }

    public BindingSet copy() {
        BindingSet copy = new BindingSet();
        for (Map.Entry<Action, List<Binding>> entry : bindings.entrySet()) {
            copy.bindings.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }

    /** Serializes to the text format read by {@link BindingLoader}. */
    public String toText() {
        StringBuilder text = new StringBuilder();
        for (Map.Entry<Action, List<Binding>> entry : bindings.entrySet()) {
            text.append(entry.getKey().name()).append('=');
            List<Binding> list = entry.getValue();
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) {
                    text.append(',');
                }
                text.append(list.get(i).toText());
            }
            text.append('\n');
        }
        return text.toString();
    }
}