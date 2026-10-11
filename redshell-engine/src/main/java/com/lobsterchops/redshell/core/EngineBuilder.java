package com.lobsterchops.redshell.core;

import com.lobsterchops.redshell.core.config.ConfigRegistry;
import com.lobsterchops.redshell.core.config.EngineConfig;
import com.lobsterchops.redshell.core.context.EngineContext;
import com.lobsterchops.redshell.core.lifecycle.EngineSystem;
import com.lobsterchops.redshell.core.lifecycle.RenderBracket;
import com.lobsterchops.redshell.core.lifecycle.SystemRegistry;
import com.lobsterchops.redshell.core.time.GameClock;
import com.lobsterchops.redshell.debug.log.Log;

/**
 * Assembles the engine in a fixed order: clock, configuration registry and log first, then the
 * services and systems in the order they are added here.
 */
public final class EngineBuilder {

    private final EngineConfig engineConfig;
    private final Log log;
    private final ConfigRegistry configs = new ConfigRegistry();
    private final EngineContext context;
    private final SystemRegistry systems;
    private RenderBracket renderBracket = new NoRenderBracket();

    public EngineBuilder(EngineConfig engineConfig, Log log) {
        this.engineConfig = engineConfig;
        this.log = log;
        configs.register(EngineConfig.REGISTRY_NAME, engineConfig);
        GameClock clock = new GameClock(engineConfig.maxFrameSeconds());
        this.context = new EngineContext(clock, engineConfig, configs, log);
        this.systems = new SystemRegistry(log);
    }

    public EngineBuilder config(String name, Object config) {
        configs.register(name, config);
        return this;
    }

    public <T> EngineBuilder service(Class<T> type, T service) {
        context.registerService(type, service);
        return this;
    }

    public EngineBuilder renderBracket(RenderBracket renderBracket) {
        this.renderBracket = renderBracket;
        return this;
    }

    /** Adds a system. Systems initialize in the order they are added. */
    public EngineBuilder system(EngineSystem system) {
        systems.register(system);
        return this;
    }

    public Engine build() {
        context.seal();
        log.debug("EngineBuilder", "Building engine (fixed timestep: " + engineConfig.fixedTimestepEnabled() + ")");
        return new Engine(context, systems, renderBracket);
    }

    private static final class NoRenderBracket implements RenderBracket {
        @Override
        public void beginRender() {
        }

        @Override
        public void endRender() {
        }
    }
}
