package com.lobsterchops.redshell.input.action;

/** What game code sees for one action this frame: pressed, held, released and an analog value. */
public final class ActionState {

    private boolean pressed;
    private boolean held;
    private boolean released;
    private float analog;
    private boolean rawDown;

    /** True only on the frame the action went down. */
    public boolean pressed() {
        return pressed;
    }

    public boolean held() {
        return held;
    }

    /** True only on the frame the action went up, or was disabled while held. */
    public boolean released() {
        return released;
    }

    /** 0 to 1 for digital inputs; the axis magnitude for analog ones. Zero while the action is disabled. */
    public float analog() {
        return analog;
    }

    /** Called by the input service once per frame. */
    public void update(boolean down, float value, boolean active) {
        boolean wasHeld = held;
        pressed = active && down && !rawDown;
        held = active && down;
        released = wasHeld && !held;
        analog = active ? value : 0f;
        rawDown = down;
    }
}