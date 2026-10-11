package com.lobsterchops.redshell.input.device;

import com.badlogic.gdx.Gdx;
import com.lobsterchops.redshell.input.binding.Binding;

/** Mouse button adapter. */
public final class MouseDevice implements InputDevice {

    @Override
    public boolean supports(Binding.Type type) {
        return type == Binding.Type.MOUSE_BUTTON;
    }

    @Override
    public float value(Binding binding) {
        return Gdx.input.isButtonPressed(binding.code()) ? 1f : 0f;
    }
}