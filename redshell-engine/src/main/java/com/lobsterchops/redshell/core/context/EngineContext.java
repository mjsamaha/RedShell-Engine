package com.lobsterchops.redshell.core.context;

import com.lobsterchops.redshell.core.config.ConfigRegistry;
import com.lobsterchops.redshell.core.config.EngineConfig;
import com.lobsterchops.redshell.core.time.GameClock;
import com.lobsterchops.redshell.debug.log.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Explicit, small access point for engine-managed code. It always exposes the clock, configuration
 * and log. Services owned by higher layers (renderer now; assets, input, audio, events later) are
 * registered once by the engine builder and then sealed, so game code cannot turn this into a
 * service locator. Every new service must be justified in its own Javadoc.
 */
public final class EngineContext {

    private final GameClock clock;
    private final EngineConfig config;
    private final ConfigRegistry configs;
    private final Log log;
    private final Map<Class<?>, Object> services = new HashMap<>();
    private boolean sealed;

    public EngineContext(GameClock clock, EngineConfig config, ConfigRegistry configs, Log log) {
        this.clock = clock;
        this.config = config;
        this.configs = configs;
        this.log = log;
    }

    public GameClock clock() {
        return clock;
    }

    public EngineConfig config() {
        return config;
    }

    public ConfigRegistry configs() {
        return configs;
    }

    public Log log() {
        return log;
    }

    /** Registers a service. Only possible before the context is sealed by the engine builder. */
    public <T> void registerService(Class<T> type, T service) {
        if (sealed) {
            throw new IllegalStateException("Services can only be registered while the engine is being built");
        }
        if (type == null || service == null) {
            throw new IllegalArgumentException("type and service must not be null");
        }
        if (services.containsKey(type)) {
            throw new IllegalStateException("Service already registered: " + type.getName());
        }
        services.put(type, service);
    }

    public void seal() {
        sealed = true;
    }

    public boolean hasService(Class<?> type) {
        return services.containsKey(type);
    }

    // Class.cast is not available under GWT; the map is keyed by type, so the cast is safe.
    @SuppressWarnings("unchecked")
    public <T> T service(Class<T> type) {
        Object service = services.get(type);
        if (service == null) {
            throw new IllegalStateException("Service not registered: " + type.getName());
        }
        return (T) service;
    }
}
