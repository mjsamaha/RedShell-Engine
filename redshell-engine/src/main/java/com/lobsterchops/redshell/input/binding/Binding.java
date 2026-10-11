package com.lobsterchops.redshell.input.binding;

import com.badlogic.gdx.Input;

/** Maps one physical input (a key, mouse button, gamepad button or half-axis) to an action. */
public final class Binding {

    /** The kind of physical input. */
    public enum Type {
        KEY,
        MOUSE_BUTTON,
        GAMEPAD_BUTTON,
        GAMEPAD_AXIS_POSITIVE,
        GAMEPAD_AXIS_NEGATIVE
    }

    private final Type type;
    private final int code;

    private Binding(Type type, int code) {
        this.type = type;
        this.code = code;
    }

    public static Binding key(int keycode) {
        return new Binding(Type.KEY, keycode);
    }

    public static Binding mouseButton(int button) {
        return new Binding(Type.MOUSE_BUTTON, button);
    }

    public static Binding gamepadButton(int button) {
        return new Binding(Type.GAMEPAD_BUTTON, button);
    }

    public static Binding gamepadAxis(int axis, boolean positive) {
        return new Binding(positive ? Type.GAMEPAD_AXIS_POSITIVE : Type.GAMEPAD_AXIS_NEGATIVE, axis);
    }

    public Type type() {
        return type;
    }

    public int code() {
        return code;
    }

    /** Text form used by {@link BindingLoader}, for example {@code KEY:W} or {@code MOUSE:0}. */
    public String toText() {
        switch (type) {
            case KEY:
                return "KEY:" + Input.Keys.toString(code);
            case MOUSE_BUTTON:
                return "MOUSE:" + code;
            case GAMEPAD_BUTTON:
                return "PAD:" + code;
            case GAMEPAD_AXIS_POSITIVE:
                return "AXIS+:" + code;
            default:
                return "AXIS-:" + code;
        }
    }

    /** Parses the text form produced by {@link #toText()}. */
    public static Binding parse(String text) {
        int colon = text.indexOf(':');
        if (colon <= 0 || colon == text.length() - 1) {
            throw new IllegalArgumentException("Invalid binding: '" + text + "'");
        }
        String kind = text.substring(0, colon).trim();
        String value = text.substring(colon + 1).trim();
        try {
            switch (kind) {
                case "KEY":
                    int keycode = Input.Keys.valueOf(value);
                    if (keycode == -1) {
                        // LibGDX names keys like "Space"; accept any letter case in binding files.
                        keycode = Input.Keys.valueOf(value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase());
                    }
                    if (keycode == -1) {
                        throw new IllegalArgumentException("Unknown key: '" + value + "'");
                    }
                    return key(keycode);
                case "MOUSE":
                    return mouseButton(Integer.parseInt(value));
                case "PAD":
                    return gamepadButton(Integer.parseInt(value));
                case "AXIS+":
                    return gamepadAxis(Integer.parseInt(value), true);
                case "AXIS-":
                    return gamepadAxis(Integer.parseInt(value), false);
                default:
                    throw new IllegalArgumentException("Unknown binding kind: '" + kind + "'");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid binding number in '" + text + "'");
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Binding)) {
            return false;
        }
        Binding binding = (Binding) other;
        return type == binding.type && code == binding.code;
    }

    @Override
    public int hashCode() {
        return type.hashCode() * 31 + code;
    }

    @Override
    public String toString() {
        return toText();
    }
}