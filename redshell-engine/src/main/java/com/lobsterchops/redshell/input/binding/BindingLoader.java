package com.lobsterchops.redshell.input.binding;

import com.lobsterchops.redshell.input.action.Action;

/**
 * Reads a {@link BindingSet} from text, one action per line: {@code MOVE_LEFT=KEY:A,KEY:LEFT}.
 * Blank lines and lines starting with {@code #} are ignored.
 */
public final class BindingLoader {

    private BindingLoader() {
    }

    /** Parses the text. {@code knownActions} resolves action names; unknown names are an error. */
    public static BindingSet parse(String text, Action... knownActions) {
        BindingSet set = new BindingSet();
        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.charAt(0) == '#') {
                continue;
            }
            int equals = line.indexOf('=');
            if (equals <= 0) {
                throw new IllegalArgumentException("Line " + (i + 1) + ": expected ACTION=BINDING,...");
            }
            String name = line.substring(0, equals).trim();
            Action action = find(name, knownActions);
            if (action == null) {
                throw new IllegalArgumentException("Line " + (i + 1) + ": unknown action '" + name + "'");
            }
            String[] parts = line.substring(equals + 1).split(",");
            for (String part : parts) {
                if (!part.trim().isEmpty()) {
                    set.bind(action, Binding.parse(part.trim()));
                }
            }
        }
        return set;
    }

    private static Action find(String name, Action[] known) {
        for (Action action : known) {
            if (action.name().equals(name)) {
                return action;
            }
        }
        return null;
    }
}