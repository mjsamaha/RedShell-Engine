package com.lobsterchops.redshell.core.lifecycle;

import com.lobsterchops.redshell.core.context.EngineContext;
import com.lobsterchops.redshell.debug.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Ordered registration of {@link EngineSystem}s. Systems initialize in registration order, receive
 * every callback in that order, and are disposed in reverse order of initialization.
 */
public final class SystemRegistry {

    private static final String TAG = "SystemRegistry";

    private final Log log;
    private final List<EngineSystem> registered = new ArrayList<>();
    private final List<EngineSystem> initialized = new ArrayList<>();
    private boolean initializing;

    public SystemRegistry(Log log) {
        this.log = log;
    }

    public void register(EngineSystem system) {
        if (system == null) {
            throw new IllegalArgumentException("system must not be null");
        }
        if (initializing) {
            throw new IllegalStateException("Systems cannot be registered after initialization has started");
        }
        if (registered.contains(system)) {
            throw new IllegalStateException("System already registered: " + nameOf(system));
        }
        registered.add(system);
    }

    public void initializeAll(EngineContext context) {
        initializing = true;
        for (int i = 0; i < registered.size(); i++) {
            EngineSystem system = registered.get(i);
            log.debug(TAG, "Initializing " + nameOf(system));
            system.initialize(context);
            initialized.add(system);
        }
    }

    public void fixedUpdateAll(float fixedDeltaTime) {
        for (int i = 0; i < initialized.size(); i++) {
            initialized.get(i).fixedUpdate(fixedDeltaTime);
        }
    }

    public void updateAll(float deltaTime) {
        for (int i = 0; i < initialized.size(); i++) {
            initialized.get(i).update(deltaTime);
        }
    }

    public void renderAll(float deltaTime) {
        for (int i = 0; i < initialized.size(); i++) {
            initialized.get(i).render(deltaTime);
        }
    }

    public void resizeAll(int width, int height) {
        for (int i = 0; i < initialized.size(); i++) {
            initialized.get(i).resize(width, height);
        }
    }

    public void pauseAll() {
        for (int i = 0; i < initialized.size(); i++) {
            initialized.get(i).pause();
        }
    }

    public void resumeAll() {
        for (int i = 0; i < initialized.size(); i++) {
            initialized.get(i).resume();
        }
    }

    /** Disposes every initialized system in reverse order. A failing system does not stop the others. */
    public void disposeAll() {
        for (int i = initialized.size() - 1; i >= 0; i--) {
            EngineSystem system = initialized.get(i);
            log.debug(TAG, "Disposing " + nameOf(system));
            try {
                system.dispose();
            } catch (RuntimeException e) {
                log.error(TAG, "Failed to dispose " + nameOf(system), e);
            }
        }
        initialized.clear();
    }

    public int size() {
        return registered.size();
    }

    private static String nameOf(EngineSystem system) {
        return system.getClass().getName();
    }
}
