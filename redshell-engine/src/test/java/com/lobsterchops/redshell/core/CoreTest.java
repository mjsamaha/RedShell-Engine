package com.lobsterchops.redshell.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lobsterchops.redshell.core.config.ConfigRegistry;
import com.lobsterchops.redshell.core.config.EngineConfig;
import com.lobsterchops.redshell.core.context.EngineContext;
import com.lobsterchops.redshell.core.lifecycle.EngineSystem;
import com.lobsterchops.redshell.core.lifecycle.RenderBracket;
import com.lobsterchops.redshell.core.time.FixedStepAccumulator;
import com.lobsterchops.redshell.core.time.GameClock;
import com.lobsterchops.redshell.debug.log.Log;
import com.lobsterchops.redshell.debug.log.LogLevel;
import com.lobsterchops.redshell.debug.log.LogSink;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class CoreTest {

    private static final float EPS = 1e-5f;

    private static final class Recorder implements EngineSystem {
        private final String name;
        private final List<String> events;

        Recorder(String name, List<String> events) {
            this.name = name;
            this.events = events;
        }

        @Override
        public void initialize(EngineContext context) {
            events.add(name + ":init");
        }

        @Override
        public void fixedUpdate(float dt) {
            events.add(name + ":fixed");
        }

        @Override
        public void update(float dt) {
            events.add(name + ":update");
        }

        @Override
        public void render(float dt) {
            events.add(name + ":render");
        }

        @Override
        public void resize(int w, int h) {
            events.add(name + ":resize" + w + "x" + h);
        }

        @Override
        public void pause() {
            events.add(name + ":pause");
        }

        @Override
        public void resume() {
            events.add(name + ":resume");
        }

        @Override
        public void dispose() {
            events.add(name + ":dispose");
        }
    }

    private static final class CapturingSink implements LogSink {
        final List<String> lines = new ArrayList<>();

        @Override
        public void write(LogLevel level, String tag, String message, Throwable throwable) {
            lines.add(level + "|" + tag + "|" + message);
        }
    }

    private static RenderBracket bracket(List<String> events) {
        return new RenderBracket() {
            @Override
            public void beginRender() {
                events.add("begin");
            }

            @Override
            public void endRender() {
                events.add("end");
            }
        };
    }

    private static Engine engine(EngineConfig config, List<String> events, EngineSystem... extra) {
        EngineBuilder builder = new EngineBuilder(config, new Log(LogLevel.OFF))
                .renderBracket(bracket(events))
                .system(new Recorder("a", events))
                .system(new Recorder("b", events));
        for (EngineSystem system : extra) {
            builder.system(system);
        }
        return builder.build();
    }

    @Test
    void clockScalesPausesAndCapsStalls() {
        GameClock clock = new GameClock(0.25f);
        clock.setTimeScale(0.5f);
        clock.tick(0.1f);
        assertEquals(0.05f, clock.deltaTime(), EPS);
        assertEquals(0.1f, clock.unscaledDeltaTime(), EPS);
        clock.pause();
        clock.tick(0.1f);
        assertEquals(0f, clock.deltaTime(), 0f);
        assertEquals(0.2f, clock.unscaledTotalTime(), EPS);
        clock.resume();
        clock.tick(10f);
        assertEquals(0.125f, clock.deltaTime(), EPS);
        assertEquals(3, clock.frameCount());
        assertThrows(IllegalArgumentException.class, () -> clock.setTimeScale(-1f));
    }

    @Test
    void accumulatorProducesWholeStepsAndAlpha() {
        FixedStepAccumulator accumulator = new FixedStepAccumulator(0.1f, 0.5f);
        assertEquals(0, accumulator.advance(0.05f));
        assertEquals(1, accumulator.advance(0.06f));
        assertEquals(0.1f, accumulator.alpha(), 0.01f);
        assertEquals(5, accumulator.advance(100f));
        accumulator.reset();
        assertEquals(0f, accumulator.alpha(), 0f);
    }

    @Test
    void configRegistryStoresTypedConfigs() {
        ConfigRegistry registry = new ConfigRegistry();
        registry.register("engine", EngineConfig.defaults());
        assertTrue(registry.has("engine"));
        assertFalse(registry.has("other"));
        assertEquals(EngineConfig.defaults().logLevel(), registry.get("engine", EngineConfig.class).logLevel());
        assertThrows(RuntimeException.class, () -> registry.get("missing", String.class));
    }

    @Test
    void engineConfigDefaultsAndValidation() {
        EngineConfig defaults = EngineConfig.defaults();
        assertFalse(defaults.fixedTimestepEnabled());
        assertEquals(1f / 60f, defaults.fixedStepSeconds(), EPS);
        assertThrows(RuntimeException.class, () -> EngineConfig.builder().fixedStepSeconds(0f).build());
    }

    @Test
    void servicesAreTypedAndSealed() {
        Engine engine = new EngineBuilder(EngineConfig.defaults(), new Log(LogLevel.OFF))
                .service(String.class, "hello").build();
        assertSame("hello", engine.context().service(String.class));
        assertTrue(engine.context().hasService(String.class));
        assertThrows(RuntimeException.class, () -> engine.context().service(Integer.class));
        assertThrows(RuntimeException.class, () -> engine.context().registerService(Integer.class, 1));
    }

    @Test
    void lifecycleRunsInRegistrationOrderAndDisposesInReverse() {
        List<String> events = new ArrayList<>();
        Engine engine = engine(EngineConfig.defaults(), events);
        engine.start();
        engine.resize(800, 600);
        engine.resize(0, 600);
        engine.pause();
        engine.resume();
        engine.frame(0.016f);
        engine.shutdown();
        engine.shutdown();
        assertEquals(Arrays.asList(
                "a:init", "b:init",
                "a:resize800x600", "b:resize800x600",
                "a:pause", "b:pause",
                "a:resume", "b:resume",
                "a:update", "b:update",
                "begin", "a:render", "b:render", "end",
                "b:dispose", "a:dispose"), events);
    }

    @Test
    void fixedUpdateRunsOnlyWhenEnabled() {
        List<String> off = new ArrayList<>();
        Engine disabled = engine(EngineConfig.defaults(), off);
        disabled.start();
        disabled.frame(0.1f);
        assertFalse(off.contains("a:fixed"));

        List<String> on = new ArrayList<>();
        Engine enabled = engine(EngineConfig.builder().fixedTimestep(true).fixedStepSeconds(0.05f).build(), on);
        enabled.start();
        enabled.frame(0.11f);
        assertEquals(2, on.stream().filter("a:fixed"::equals).count());
        assertTrue(on.indexOf("a:fixed") < on.indexOf("a:update"));
    }

    @Test
    void failingFrameIsReportedOnceRethrownAndStillEndsRender() {
        List<String> events = new ArrayList<>();
        CapturingSink sink = new CapturingSink();
        EngineSystem failing = new EngineSystem() {
            @Override
            public void render(float dt) {
                throw new IllegalStateException("boom");
            }
        };
        Engine engine = new EngineBuilder(EngineConfig.defaults(), new Log(LogLevel.DEBUG, sink))
                .renderBracket(bracket(events))
                .system(failing)
                .build();
        engine.start();
        assertThrows(IllegalStateException.class, () -> engine.frame(0.016f));
        assertEquals(Arrays.asList("begin", "end"), events);
        assertEquals(1, sink.lines.stream().filter(l -> l.startsWith("ERROR")).count());
    }

    @Test
    void failingDisposeDoesNotStopOthers() {
        List<String> events = new ArrayList<>();
        EngineSystem failing = new EngineSystem() {
            @Override
            public void dispose() {
                throw new IllegalStateException("dispose boom");
            }
        };
        Engine engine = engine(EngineConfig.defaults(), events, failing);
        engine.start();
        engine.shutdown();
        assertTrue(events.contains("a:dispose"));
        assertTrue(events.contains("b:dispose"));
    }

    @Test
    void frameBeforeStartIsRejected() {
        Engine engine = engine(EngineConfig.defaults(), new ArrayList<>());
        assertThrows(IllegalStateException.class, () -> engine.frame(0.016f));
    }

    @Test
    void logFiltersByLevelAndSupportsSinks() {
        CapturingSink sink = new CapturingSink();
        Log log = new Log(LogLevel.WARN, sink);
        log.debug("T", "no");
        log.info("T", "no");
        log.warn("T", "warn");
        log.error("T", "err", new RuntimeException("x"));
        assertEquals(Arrays.asList("WARN|T|warn", "ERROR|T|err"), sink.lines);
        log.setLevel(LogLevel.OFF);
        log.error("T", "silent");
        assertEquals(2, sink.lines.size());
        log.removeSink(sink);
        log.setLevel(LogLevel.DEBUG);
        log.debug("T", "gone");
        assertEquals(2, sink.lines.size());
    }
}
