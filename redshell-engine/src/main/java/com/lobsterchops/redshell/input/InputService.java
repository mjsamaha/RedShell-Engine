package com.lobsterchops.redshell.input;

import com.lobsterchops.redshell.core.lifecycle.EngineSystem;
import com.lobsterchops.redshell.input.action.Action;
import com.lobsterchops.redshell.input.action.ActionState;
import com.lobsterchops.redshell.input.binding.Binding;
import com.lobsterchops.redshell.input.binding.BindingSet;
import com.lobsterchops.redshell.input.context.ContextStack;
import com.lobsterchops.redshell.input.device.InputDevice;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Polls devices once per frame and exposes {@link ActionState}s. Game code reads actions only.
 * Actions outside the active {@link ContextStack} contexts read as idle.
 */
public final class InputService implements EngineSystem {

    private static final float DOWN_THRESHOLD = 0.5f;

    private final InputDevice[] devices;
    private final ContextStack contexts = new ContextStack();
    private final Map<Action, ActionState> states = new HashMap<>();
    private final ActionState idle = new ActionState();
    private BindingSet bindings;
    private boolean pointerCaptured;

    public InputService(BindingSet bindings, InputDevice... devices) {
        this.bindings = bindings;
        this.devices = devices;
    }

    @Override
    public void update(float deltaTime) {
        poll();
    }

    /** Reads the devices and refreshes every bound action. Called by the engine each frame. */
    public void poll() {
        for (Action action : bindings.actions()) {
            float value = 0f;
            List<Binding> list = bindings.bindingsFor(action);
            for (int i = 0; i < list.size(); i++) {
                value = Math.max(value, read(list.get(i)));
            }
            ActionState state = states.get(action);
            if (state == null) {
                state = new ActionState();
                states.put(action, state);
            }
            state.update(value >= DOWN_THRESHOLD, value, contexts.isActive(action));
        }
    }

    private float read(Binding binding) {
        if (pointerCaptured && binding.type() == Binding.Type.MOUSE_BUTTON) {
            return 0f;
        }
        for (InputDevice device : devices) {
            if (device.supports(binding.type())) {
                return device.value(binding);
            }
        }
        return 0f;
    }

    public ActionState state(Action action) {
        ActionState state = states.get(action);
        return state == null ? idle : state;
    }

    public boolean pressed(Action action) {
        return state(action).pressed();
    }

    public boolean held(Action action) {
        return state(action).held();
    }

    public boolean released(Action action) {
        return state(action).released();
    }

    public float analog(Action action) {
        return state(action).analog();
    }

    /** While true (the pointer is over UI), mouse-button bindings read as released so clicks do not leak into gameplay. */
    public void setPointerCaptured(boolean captured) {
        this.pointerCaptured = captured;
    }

    public ContextStack contexts() {
        return contexts;
    }

    public BindingSet bindings() {
        return bindings;
    }

    /** Swaps the whole profile, for example after loading saved bindings. */
    public void setBindings(BindingSet bindings) {
        this.bindings = bindings;
        states.clear();
    }
}