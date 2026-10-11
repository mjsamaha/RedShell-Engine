package com.lobsterchops.redshell.debug;

import static org.junit.jupiter.api.Assertions.*;

import com.lobsterchops.redshell.assets.catalog.AssetCatalog;
import com.lobsterchops.redshell.assets.catalog.AssetType;
import com.lobsterchops.redshell.assets.validation.AssetValidator;
import com.lobsterchops.redshell.core.lifecycle.EngineSystem;
import com.lobsterchops.redshell.core.lifecycle.ScopeListener;
import com.lobsterchops.redshell.core.lifecycle.SystemRegistry;
import com.lobsterchops.redshell.debug.inspector.EntityInspector;
import com.lobsterchops.redshell.debug.log.Log;
import com.lobsterchops.redshell.debug.log.LogLevel;
import com.lobsterchops.redshell.debug.log.MemoryLogSink;
import com.lobsterchops.redshell.debug.profiler.FrameStats;
import com.lobsterchops.redshell.debug.profiler.Profiler;
import com.lobsterchops.redshell.ecs.World;
import com.lobsterchops.redshell.ecs.system.EntitySystem;
import com.lobsterchops.redshell.entity.Entity;
import com.lobsterchops.redshell.entity.EntityTag;
import com.lobsterchops.redshell.entity.component.ColliderComponent;
import com.lobsterchops.redshell.entity.component.TransformComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DebugToolsTest {

    @Test
    void frameStatsRollsOverItsWindow() {
        FrameStats stats = new FrameStats(3);
        stats.record(10f);
        stats.record(20f);
        stats.record(30f);
        assertEquals(20f, stats.average(), 1e-4);
        stats.record(40f);
        assertEquals(30f, stats.average(), 1e-4);
        assertEquals(40f, stats.max(), 1e-4);
        assertEquals(20f, stats.min(), 1e-4);
        assertEquals(40f, stats.latest(), 1e-4);
        assertEquals(3, stats.count());
    }

    @Test
    void memorySinkKeepsNewestLinesOldestFirst() {
        MemoryLogSink sink = new MemoryLogSink(3);
        Log log = new Log(LogLevel.DEBUG, sink);
        for (int i = 1; i <= 5; i++) {
            log.info("T", "m" + i);
        }
        List<String> lines = sink.recent(10);
        assertEquals(3, lines.size());
        assertTrue(lines.get(0).endsWith("m3"));
        assertTrue(lines.get(2).endsWith("m5"));
        assertEquals(1, sink.recent(1).size());
    }

    @Test
    void registryReportsEverySystemScope() {
        final List<String> events = new ArrayList<>();
        SystemRegistry registry = new SystemRegistry(new Log(LogLevel.OFF));
        registry.setScopeListener(new ScopeListener() {
            public void begin(String scope) {
                events.add("b:" + scope);
            }

            public void end(String scope) {
                events.add("e:" + scope);
            }
        });
        registry.register(new EngineSystem() {
        });
        registry.initializeAll(null);
        registry.updateAll(0.1f);
        registry.renderAll(0.1f);
        assertEquals(4, events.size());
        assertTrue(events.get(0).startsWith("b:update:"));
        assertTrue(events.get(3).startsWith("e:render:"));
    }

    @Test
    void profilerSumsScopesPerFrame() {
        Profiler profiler = new Profiler(10);
        profiler.begin("a");
        profiler.end("a");
        profiler.endFrame();
        assertNotNull(profiler.scope("a"));
        assertEquals(1, profiler.scope("a").stats().count());
        assertEquals(1, profiler.scopes().size());
    }

    @Test
    void worldReportsItsSystemsToTheListener() {
        final Set<String> scopes = new HashSet<>();
        World world = new World();
        world.setScopeListener(new ScopeListener() {
            public void begin(String scope) {
                scopes.add(scope);
            }

            public void end(String scope) {
            }
        });
        world.addSystem(new EntitySystem(1) {
        });
        world.update(0.1f);
        assertEquals(1, scopes.size());
        assertTrue(scopes.iterator().next().startsWith("world.update:"));
    }

    @Test
    void inspectorListsComponentsAndTagsWithoutChangingTheWorld() {
        World world = new World();
        Entity e = world.createEntity();
        e.add(new TransformComponent(1.5f, 2f));
        e.add(ColliderComponent.box(10, 20, 3));
        e.addTag(EntityTag.PLAYER);
        world.createEntity();
        EntityInspector inspector = new EntityInspector();
        List<String> lines = inspector.lines(world);
        assertTrue(lines.get(0).startsWith("Entity 1/2"));
        assertTrue(lines.toString().contains("Transform pos=(1.5, 2.0)"));
        assertTrue(lines.toString().contains("Collider RECTANGLE 10.0x20.0 layer=3"));
        assertTrue(lines.toString().contains("PLAYER"));
        inspector.next(world);
        assertTrue(inspector.lines(world).get(0).startsWith("Entity 2/2"));
        assertEquals(2, world.entityCount());
        assertTrue(inspector.selectTagged(world, EntityTag.PLAYER));
        assertEquals(0, inspector.selectedIndex());
    }

    @Test
    void validatorReportsMissingUnsupportedAndSharedFiles() {
        AssetCatalog catalog = new AssetCatalog()
                .register("ok", "a.png", AssetType.TEXTURE)
                .register("missing", "gone.png", AssetType.TEXTURE)
                .register("badaudio", "song.flac", AssetType.MUSIC)
                .register("twin", "a.png", AssetType.TEXTURE);
        final Set<String> existing = new HashSet<>(Arrays.asList("a.png", "song.flac"));
        List<String> problems = new AssetValidator(existing::contains).validate(catalog);
        assertEquals(3, problems.size());
        assertTrue(problems.toString().contains("Missing file for asset 'missing'"));
        assertTrue(problems.toString().contains("Unsupported file type for MUSIC asset 'badaudio'"));
        assertTrue(problems.toString().contains("use the same file: a.png"));
    }

    @Test
    void validCatalogHasNoProblems() {
        AssetCatalog catalog = new AssetCatalog()
                .register("a", "a.png", AssetType.TEXTURE)
                .register("s", "s.wav", AssetType.SOUND);
        assertTrue(new AssetValidator(path -> true).validate(catalog).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> catalog.register("a", "b.png", AssetType.TEXTURE));
    }
}