package com.lobsterchops.redshell.ecs.system;

/** Suggested priorities for the built-in systems. Game systems pick values between them. */
public final class SystemPriority {

    public static final int INPUT = 0;
    public static final int MOVEMENT = 100;
    public static final int ANIMATION = 200;
    public static final int LIFETIME = 300;
    public static final int CAMERA = 400;
    public static final int RENDER = 1000;

    private SystemPriority() {
    }
}