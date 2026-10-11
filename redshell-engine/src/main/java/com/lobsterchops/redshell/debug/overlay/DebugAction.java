package com.lobsterchops.redshell.debug.overlay;

import com.lobsterchops.redshell.input.action.Action;
import com.lobsterchops.redshell.input.binding.BindingLoader;
import com.lobsterchops.redshell.input.binding.BindingSet;

/** The overlay's own actions. They live in a separate input service, so no game input context can block them. */
public enum DebugAction implements Action {
    TOGGLE_OVERLAY,
    TOGGLE_COLLIDERS,
    TOGGLE_SYSTEMS,
    TOGGLE_INSPECTOR,
    INSPECT_NEXT,
    INSPECT_PREVIOUS;

    /** F3 overlay, F4 collider bounds, F5 system timings, F6 inspector, F7/F8 select entity. */
    public static BindingSet defaultBindings() {
        return BindingLoader.parse(
                "TOGGLE_OVERLAY=KEY:F3\n"
                + "TOGGLE_COLLIDERS=KEY:F4\n"
                + "TOGGLE_SYSTEMS=KEY:F5\n"
                + "TOGGLE_INSPECTOR=KEY:F6\n"
                + "INSPECT_NEXT=KEY:F7\n"
                + "INSPECT_PREVIOUS=KEY:F8\n",
                values());
    }
}