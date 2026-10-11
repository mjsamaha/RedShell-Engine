package com.lobsterchops.redshell.debug.overlay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.lobsterchops.redshell.assets.AssetService;
import com.lobsterchops.redshell.core.context.EngineContext;
import com.lobsterchops.redshell.core.lifecycle.EngineSystem;
import com.lobsterchops.redshell.debug.inspector.EntityInspector;
import com.lobsterchops.redshell.debug.log.MemoryLogSink;
import com.lobsterchops.redshell.debug.profiler.FrameStats;
import com.lobsterchops.redshell.debug.profiler.Profiler;
import com.lobsterchops.redshell.ecs.World;
import com.lobsterchops.redshell.ecs.query.Query;
import com.lobsterchops.redshell.entity.Entity;
import com.lobsterchops.redshell.entity.component.ColliderComponent;
import com.lobsterchops.redshell.entity.component.TransformComponent;
import com.lobsterchops.redshell.input.InputService;
import com.lobsterchops.redshell.input.device.KeyboardDevice;
import com.lobsterchops.redshell.rendering.Renderer2D;
import com.lobsterchops.redshell.scene.Scene;
import com.lobsterchops.redshell.scene.SceneManager;
import java.util.ArrayList;
import java.util.List;

/**
 * In-game debug overlay, drawn after everything else. F3 toggles FPS, frame time, entity count, draw calls, memory
 * and recent log lines; F4 draws collider bounds; F5 lists every system's rolling update/render time; F6 opens the
 * read-only entity inspector (F7/F8 change the selection). The keys are {@link DebugAction}s read by the overlay's
 * own input service, so game input contexts and rebinding never interfere.
 *
 * <p>Text needs the UI bitmap font, so it appears once a scene that loaded that font is active. The overlay
 * refreshes its text a few times per second to keep its own cost and garbage low.
 */
public final class DebugOverlay implements EngineSystem {

    private static final float REFRESH_SECONDS = 0.25f;
    private static final int LOG_LINES = 6;
    private static final int PANEL_WIDTH = 380;
    private static final Color BACKGROUND = new Color(0f, 0f, 0f, 0.6f);

    private final String fontAssetId;
    private final Profiler profiler;
    private final MemoryLogSink logs;
    private final FrameStats frameStats = new FrameStats(120);
    private final EntityInspector inspector = new EntityInspector();
    private final Query colliderQuery = Query.all(TransformComponent.class, ColliderComponent.class);
    private final List<String> statLines = new ArrayList<>();
    private final List<String> systemLines = new ArrayList<>();
    private final List<String> inspectorLines = new ArrayList<>();
    private final List<String> logLines = new ArrayList<>();
    private InputService input;
    private AssetService assets;
    private SceneManager scenes;
    private Renderer2D renderer;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private OrthographicCamera screenCamera;
    private boolean overlayVisible;
    private boolean collidersVisible;
    private boolean systemsVisible;
    private boolean inspectorVisible;
    private float sinceRefresh = REFRESH_SECONDS;

    public DebugOverlay(String fontAssetId, Profiler profiler, MemoryLogSink logs) {
        this.fontAssetId = fontAssetId;
        this.profiler = profiler;
        this.logs = logs;
    }

    @Override
    public void initialize(EngineContext context) {
        assets = context.service(AssetService.class);
        scenes = context.service(SceneManager.class);
        renderer = context.hasService(Renderer2D.class) ? context.service(Renderer2D.class) : null;
        input = new InputService(DebugAction.defaultBindings(), new KeyboardDevice());
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        screenCamera = new OrthographicCamera();
    }

    public boolean isVisible() {
        return overlayVisible;
    }

    @Override
    public void update(float deltaTime) {
        input.update(deltaTime);
        if (input.pressed(DebugAction.TOGGLE_OVERLAY)) {
            overlayVisible = !overlayVisible;
        }
        if (input.pressed(DebugAction.TOGGLE_COLLIDERS)) {
            collidersVisible = !collidersVisible;
        }
        if (input.pressed(DebugAction.TOGGLE_SYSTEMS)) {
            systemsVisible = !systemsVisible;
        }
        if (input.pressed(DebugAction.TOGGLE_INSPECTOR)) {
            inspectorVisible = !inspectorVisible;
        }
        World world = activeWorld();
        if (world != null && inspectorVisible) {
            if (input.pressed(DebugAction.INSPECT_NEXT)) {
                inspector.next(world);
                sinceRefresh = REFRESH_SECONDS;
            }
            if (input.pressed(DebugAction.INSPECT_PREVIOUS)) {
                inspector.previous(world);
                sinceRefresh = REFRESH_SECONDS;
            }
        }
        sinceRefresh += deltaTime;
    }

    @Override
    public void render(float deltaTime) {
        frameStats.record(Gdx.graphics.getDeltaTime() * 1000f);
        if (overlayVisible || collidersVisible) {
            if (renderer != null) {
                renderer.flush();
            }
            if (collidersVisible && renderer != null) {
                drawColliders();
            }
            if (overlayVisible) {
                if (sinceRefresh >= REFRESH_SECONDS) {
                    sinceRefresh = 0f;
                    refreshText();
                }
                drawText();
            }
        }
        profiler.endFrame();
    }

    private void refreshText() {
        World world = activeWorld();
        Scene top = scenes.current();
        float average = frameStats.average();
        statLines.clear();
        statLines.add("FPS " + Gdx.graphics.getFramesPerSecond() + "  frame " + round(average) + " ms (max "
                + round(frameStats.max()) + ")");
        statLines.add("Entities " + (world == null ? 0 : world.entityCount()) + "  Scene "
                + (top == null ? "-" : top.name()));
        statLines.add("Draw calls " + (renderer == null ? 0 : renderer.lastFrameRenderCalls()) + "  Heap "
                + Gdx.app.getJavaHeap() / (1024 * 1024) + " MB");
        statLines.add("F3 overlay  F4 bounds  F5 systems  F6 inspector");

        systemLines.clear();
        if (systemsVisible) {
            systemLines.add("Systems (avg ms)");
            List<Profiler.Scope> scopes = profiler.scopes();
            for (int i = 0; i < scopes.size(); i++) {
                systemLines.add(scopes.get(i).name() + "  " + round(scopes.get(i).stats().average()));
            }
        }

        inspectorLines.clear();
        if (inspectorVisible) {
            if (world == null) {
                inspectorLines.add("Inspector: no scene");
            } else {
                inspectorLines.add("Inspector (F7/F8 select)");
                inspectorLines.addAll(inspector.lines(world));
            }
        }

        logLines.clear();
        logLines.addAll(logs.recent(LOG_LINES));
    }

    private void drawColliders() {
        shapes.setProjectionMatrix(renderer.camera().combined());
        shapes.begin(ShapeRenderer.ShapeType.Line);
        List<Scene> stack = scenes.stack().scenes();
        for (int s = 0; s < stack.size(); s++) {
            World world = stack.get(s).world();
            if (world == null) {
                continue;
            }
            List<Entity> entities = world.query(colliderQuery);
            for (int i = 0; i < entities.size(); i++) {
                Entity entity = entities.get(i);
                ColliderComponent collider = entity.get(ColliderComponent.class);
                TransformComponent transform = entity.get(TransformComponent.class);
                float cx = transform.worldX() + collider.offsetX;
                float cy = transform.worldY() + collider.offsetY;
                shapes.setColor(collider.trigger ? Color.YELLOW : Color.GREEN);
                if (collider.shape == ColliderComponent.Shape.CIRCLE) {
                    shapes.circle(cx, cy, collider.radius, 16);
                } else {
                    shapes.rect(cx - collider.width / 2f, cy - collider.height / 2f, collider.width, collider.height);
                }
            }
        }
        shapes.end();
    }

    private void drawText() {
        if (!assets.isLoaded(fontAssetId)) {
            return;
        }
        BitmapFont font = assets.get(fontAssetId, BitmapFont.class);
        int width = Gdx.graphics.getBackBufferWidth();
        int height = Gdx.graphics.getBackBufferHeight();
        Gdx.gl.glViewport(0, 0, width, height);
        screenCamera.setToOrtho(false, width, height);
        screenCamera.update();
        float lineHeight = font.getLineHeight();
        float top = height - 6f;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.setProjectionMatrix(screenCamera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(BACKGROUND);
        float leftLines = statLines.size() + inspectorLines.size();
        shapes.rect(4f, top - leftLines * lineHeight - 4f, PANEL_WIDTH, leftLines * lineHeight + 8f);
        if (!systemLines.isEmpty()) {
            shapes.rect(width - PANEL_WIDTH - 4f, top - systemLines.size() * lineHeight - 4f, PANEL_WIDTH,
                    systemLines.size() * lineHeight + 8f);
        }
        if (!logLines.isEmpty()) {
            shapes.rect(4f, 4f, width - 8f, logLines.size() * lineHeight + 8f);
        }
        shapes.end();

        Color previous = new Color(font.getColor());
        font.setColor(Color.WHITE);
        batch.setProjectionMatrix(screenCamera.combined);
        batch.begin();
        float y = top;
        y = drawLines(font, statLines, 8f, y, lineHeight);
        drawLines(font, inspectorLines, 8f, y, lineHeight);
        drawLines(font, systemLines, width - PANEL_WIDTH, top, lineHeight);
        drawLines(font, logLines, 8f, logLines.size() * lineHeight + 8f, lineHeight);
        batch.end();
        font.setColor(previous);
    }

    private float drawLines(BitmapFont font, List<String> lines, float x, float y, float lineHeight) {
        for (int i = 0; i < lines.size(); i++) {
            font.draw(batch, lines.get(i), x, y);
            y -= lineHeight;
        }
        return y;
    }

    private World activeWorld() {
        List<Scene> stack = scenes.stack().scenes();
        for (int i = stack.size() - 1; i >= 0; i--) {
            World world = stack.get(i).world();
            if (world != null && world.entityCount() > 0) {
                return world;
            }
        }
        return null;
    }

    private static String round(float value) {
        return String.valueOf(Math.round(value * 100f) / 100f);
    }

    @Override
    public void dispose() {
        if (batch != null) {
            batch.dispose();
        }
        if (shapes != null) {
            shapes.dispose();
        }
    }
}