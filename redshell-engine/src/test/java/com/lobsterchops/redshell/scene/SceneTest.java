package com.lobsterchops.redshell.scene;

import static org.junit.jupiter.api.Assertions.*;

import com.lobsterchops.redshell.assets.AssetService;
import com.lobsterchops.redshell.assets.catalog.AssetCatalog;
import com.lobsterchops.redshell.assets.catalog.AssetGroup;
import com.lobsterchops.redshell.assets.catalog.AssetType;
import com.lobsterchops.redshell.debug.log.Log;
import com.lobsterchops.redshell.debug.log.LogLevel;
import com.lobsterchops.redshell.events.EventBus;
import com.lobsterchops.redshell.input.InputService;
import com.lobsterchops.redshell.input.action.Action;
import com.lobsterchops.redshell.input.binding.BindingSet;
import com.lobsterchops.redshell.input.context.InputContext;
import com.lobsterchops.redshell.scene.stack.SceneStack;
import com.lobsterchops.redshell.scene.transition.FadeTransition;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SceneTest {

    enum Act implements Action { GO }

    static final class FakeAssets implements AssetService {
        final AssetCatalog catalog = new AssetCatalog().register("tex", "tex.png", AssetType.TEXTURE);
        final AssetGroup group = catalog.defineGroup("g", "tex");
        int loaded;
        int unloaded;

        @Override public AssetCatalog catalog() { return catalog; }
        @Override public void load(AssetGroup g) { loaded++; }
        @Override public void unload(AssetGroup g) { unloaded++; }
        @Override public boolean update() { return true; }
        @Override public float progress() { return 1f; }
        @Override public void finishLoading() { }
        @Override public boolean isLoaded(String id) { return loaded > unloaded; }
        @Override public boolean isLoaded(AssetGroup g) { return loaded > unloaded; }
        @Override public int referenceCount(String id) { return loaded - unloaded; }
        @Override public <T> T get(String id, Class<T> type) { return null; }
    }

    static final class Probe extends Scene {
        final List<String> calls;
        final String id;
        final SceneFlags flags;
        final AssetGroup group;
        final InputContext context = new InputContext("probe", true, Act.GO);

        Probe(String id, List<String> calls, SceneFlags flags, AssetGroup group) {
            this.id = id;
            this.calls = calls;
            this.flags = flags;
            this.group = group;
        }

        @Override public String name() { return id; }
        @Override public SceneFlags flags() { return flags; }
        @Override public AssetGroup assets() { return group; }
        @Override public InputContext inputContext() { return context; }
        @Override protected void onEnter() { calls.add(id + ":enter"); }
        @Override protected void onUpdate(float dt) { calls.add(id + ":update"); }
        @Override protected void onRender(float dt) { calls.add(id + ":render"); }
        @Override protected void onExit() { calls.add(id + ":exit"); }
        @Override protected void onDispose() { calls.add(id + ":dispose"); }
    }

    private final FakeAssets assets = new FakeAssets();
    private final InputService input = new InputService(new BindingSet());
    private final Log log = new Log(LogLevel.OFF);
    private final SceneContext context =
            new SceneContext(null, assets, input, new EventBus(log), null, null, null, log);

    @Test
    void opaqueSceneHidesTheOneBelow() {
        List<String> calls = new ArrayList<>();
        SceneStack stack = new SceneStack(context);
        stack.push(new Probe("a", calls, SceneFlags.OPAQUE, null));
        stack.push(new Probe("b", calls, SceneFlags.OPAQUE, null));
        calls.clear();
        stack.update(0.1f);
        stack.render(0.1f);
        assertEquals(List.of("b:update", "b:render"), calls);
    }

    @Test
    void overlayRendersBelowButOnlyUpdatesTop() {
        List<String> calls = new ArrayList<>();
        SceneStack stack = new SceneStack(context);
        stack.push(new Probe("a", calls, SceneFlags.OPAQUE, null));
        stack.push(new Probe("b", calls, SceneFlags.OVERLAY, null));
        calls.clear();
        stack.update(0.1f);
        stack.render(0.1f);
        assertEquals(List.of("b:update", "a:render", "b:render"), calls);
    }

    @Test
    void transparentUpdatesAndRendersBelow() {
        List<String> calls = new ArrayList<>();
        SceneStack stack = new SceneStack(context);
        stack.push(new Probe("a", calls, SceneFlags.OPAQUE, null));
        stack.push(new Probe("b", calls, SceneFlags.TRANSPARENT, null));
        calls.clear();
        stack.update(0.1f);
        assertEquals(List.of("a:update", "b:update"), calls);
    }

    @Test
    void popRunsExitThenDispose() {
        List<String> calls = new ArrayList<>();
        SceneStack stack = new SceneStack(context);
        stack.push(new Probe("a", calls, SceneFlags.OPAQUE, null));
        calls.clear();
        stack.pop();
        assertEquals(List.of("a:exit", "a:dispose"), calls);
        assertEquals(0, stack.size());
    }

    @Test
    void contextsAndAssetsAreReleasedWithTheScene() {
        List<String> calls = new ArrayList<>();
        SceneStack stack = new SceneStack(context);
        for (int i = 0; i < 5; i++) {
            stack.push(new Probe("s" + i, calls, SceneFlags.OPAQUE, assets.group));
            assertEquals(1, input.contexts().size());
            stack.pop();
            assertEquals(0, input.contexts().size());
        }
        assertEquals(5, assets.loaded);
        assertEquals(5, assets.unloaded);
    }

    @Test
    void preloadedAssetsAreNotLoadedTwice() {
        Probe scene = new Probe("a", new ArrayList<String>(), SceneFlags.OPAQUE, assets.group);
        assets.load(assets.group);
        scene.markAssetsAcquired();
        SceneStack stack = new SceneStack(context);
        stack.push(scene);
        stack.pop();
        assertEquals(1, assets.loaded);
        assertEquals(1, assets.unloaded);
    }

    @Test
    void scenesAreSingleUse() {
        SceneStack stack = new SceneStack(context);
        Probe scene = new Probe("a", new ArrayList<String>(), SceneFlags.OPAQUE, null);
        stack.push(scene);
        stack.pop();
        assertThrows(IllegalStateException.class, () -> stack.push(scene));
    }

    @Test
    void fadeReachesMidpointThenFinishes() {
        FadeTransition fade = new FadeTransition(1f);
        assertFalse(fade.reachedMidpoint());
        fade.update(0.6f);
        assertTrue(fade.reachedMidpoint());
        assertFalse(fade.isFinished());
        fade.update(0.6f);
        assertTrue(fade.isFinished());
    }
}