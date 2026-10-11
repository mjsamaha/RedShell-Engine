package com.lobsterchops.redshell.input.device;

import com.lobsterchops.redshell.input.binding.Binding;

/** Adapter for one kind of physical device. Keeps LibGDX input details out of everything else. */
public interface InputDevice {

    boolean supports(Binding.Type type);

    /** Current value of the binding: 0 or 1 for buttons, 0 to 1 for an analog axis half. */
    float value(Binding binding);
}