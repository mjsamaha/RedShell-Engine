package com.lobsterchops.redshell.input.device;

import com.badlogic.gdx.Gdx;
import com.lobsterchops.redshell.input.binding.Binding;

/** Keyboard adapter. Also covers on-screen keyboards on the web. */
public final class KeyboardDevice implements InputDevice {

    @Override
    public boolean supports(Binding.Type type) {
        return type == Binding.Type.KEY;
    }

    @Override
    public float value(Binding binding) {
        return Gdx.input.isKeyPressed(binding.code()) ? 1f : 0f;
    }
}