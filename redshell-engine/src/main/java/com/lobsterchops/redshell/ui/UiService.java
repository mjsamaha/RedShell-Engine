package com.lobsterchops.redshell.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.lobsterchops.redshell.assets.AssetService;
import com.lobsterchops.redshell.core.context.EngineContext;
import com.lobsterchops.redshell.core.lifecycle.EngineSystem;
import com.lobsterchops.redshell.input.InputRouter;
import com.lobsterchops.redshell.input.InputService;
import com.lobsterchops.redshell.rendering.Renderer2D;
import com.lobsterchops.redshell.ui.theme.UiTheme;
import java.util.ArrayList;
import java.util.List;

/**
 * Owns the Scene2D {@link Stage}. It sits above gameplay in the input chain, draws after the scene, and tells the
 * input service when the pointer is over a widget so clicks on UI do not trigger gameplay mouse actions.
 */
public final class UiService implements EngineSystem {

    /** Higher than any gameplay processor. */
    public static final int INPUT_PRIORITY = 100;

    private static final float DEFAULT_WIDTH = 640f;
    private static final float DEFAULT_HEIGHT = 360f;

    private final String fontAssetId;
    private final List<UiScreen> screens = new ArrayList<>();
    private final Vector2 pointer = new Vector2();
    private AssetService assets;
    private InputRouter router;
    private InputService input;
    private Renderer2D renderer;
    private Stage stage;
    private FitViewport viewport;
    private UiTheme theme;

    /** @param fontAssetId catalog ID of a bitmap font that is loaded whenever a screen is added */
    public UiService(String fontAssetId) {
        this.fontAssetId = fontAssetId;
    }

    @Override
    public void initialize(EngineContext context) {
        assets = context.service(AssetService.class);
        router = context.service(InputRouter.class);
        input = context.hasService(InputService.class) ? context.service(InputService.class) : null;
        float width = DEFAULT_WIDTH;
        float height = DEFAULT_HEIGHT;
        if (context.hasService(Renderer2D.class)) {
            renderer = context.service(Renderer2D.class);
            width = context.service(Renderer2D.class).worldWidth();
            height = context.service(Renderer2D.class).worldHeight();
        }
        viewport = new FitViewport(width, height);
        stage = new Stage(viewport);
        router.add(stage, INPUT_PRIORITY);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    public Stage stage() {
        return stage;
    }

    /** The shared theme. Needs the UI font to be loaded. */
    public UiTheme theme() {
        if (!assets.isLoaded(fontAssetId)) {
            throw new IllegalStateException("UI font '" + fontAssetId + "' is not loaded; add it to the scene's asset group");
        }
        // The font is reloaded when its asset group is unloaded and loaded again, so the theme follows the live instance.
        BitmapFont font = assets.get(fontAssetId, BitmapFont.class);
        if (theme == null || theme.font() != font) {
            if (theme != null) {
                theme.dispose();
            }
            theme = new UiTheme(font);
        }
        return theme;
    }

    public void add(UiScreen screen) {
        if (screens.contains(screen)) {
            return;
        }
        screens.add(screen);
        screen.attach(stage, theme());
    }

    public void remove(UiScreen screen) {
        if (screens.remove(screen)) {
            screen.detach();
        }
    }

    public boolean isPointerOverUi() {
        pointer.set(Gdx.input.getX(), Gdx.input.getY());
        stage.screenToStageCoordinates(pointer);
        return stage.hit(pointer.x, pointer.y, true) != null;
    }

    @Override
    public void update(float deltaTime) {
        stage.act(deltaTime);
        if (input != null) {
            input.setPointerCaptured(isPointerOverUi());
        }
    }

    @Override
    public void render(float deltaTime) {
        if (renderer != null) {
            renderer.flush();
        }
        viewport.apply();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) {
            viewport.update(width, height, true);
        }
    }

    @Override
    public void dispose() {
        if (router != null && stage != null) {
            router.remove(stage);
        }
        if (stage != null) {
            stage.dispose();
        }
        if (theme != null) {
            theme.dispose();
        }
    }
}