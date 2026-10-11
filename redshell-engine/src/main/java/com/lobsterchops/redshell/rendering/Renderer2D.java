package com.lobsterchops.redshell.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.lobsterchops.redshell.core.context.EngineContext;
import com.lobsterchops.redshell.core.lifecycle.EngineSystem;
import com.lobsterchops.redshell.core.lifecycle.RenderBracket;
import com.lobsterchops.redshell.rendering.camera.CoordinateConverter;
import com.lobsterchops.redshell.rendering.camera.GameCamera;
import com.lobsterchops.redshell.rendering.sprite.Sprite2D;
import com.lobsterchops.redshell.rendering.viewport.ResizeHandler;
import java.util.List;

/**
 * Clean 2D rendering on top of LibGDX's {@link SpriteBatch}. Owns the single batch, the camera and
 * the viewport, and brackets each frame's render pass (clear, apply viewport and camera, begin/end).
 *
 * <p>Register it first so it initializes first and is disposed last. Requires an OpenGL context.
 * Textures belong to the asset service; the renderer only owns its batch and a 1x1 white pixel.
 */
public final class Renderer2D implements EngineSystem, RenderBracket {

    private final RenderConfig config;
    private final SpriteBatch batch;
    private final GameCamera camera = new GameCamera();
    private final Viewport viewport;
    private final ResizeHandler resizeHandler;
    private final CoordinateConverter coordinates;
    private final RenderContext renderContext;
    private final Texture whitePixelTexture;
    private final TextureRegion whitePixel;
    private boolean drawing;

    public Renderer2D(RenderConfig config) {
        this.config = config;
        this.batch = new SpriteBatch();
        this.viewport = config.viewportPolicy().createViewport(camera.unwrap(), config.worldWidth(), config.worldHeight());
        this.resizeHandler = new ResizeHandler(viewport);
        this.coordinates = new CoordinateConverter(viewport);
        this.renderContext = new RenderContext(this, camera, coordinates);
        camera.setPosition(config.worldWidth() / 2f, config.worldHeight() / 2f);
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        whitePixelTexture = new Texture(pixmap);
        pixmap.dispose();
        whitePixel = new TextureRegion(whitePixelTexture);
    }

    @Override
    public void initialize(EngineContext context) {
        context.log().debug("Renderer2D", "World size " + config.worldWidth() + "x" + config.worldHeight()
                + ", policy " + config.viewportPolicy());
    }

    @Override
    public void resize(int width, int height) {
        resizeHandler.resize(width, height);
    }

    @Override
    public void render(float deltaTime) {
        renderContext.setDeltaTime(deltaTime);
    }

    @Override
    public void beginRender() {
        ScreenUtils.clear(config.clearRed(), config.clearGreen(), config.clearBlue(), config.clearAlpha());
        viewport.apply();
        camera.update();
        batch.setProjectionMatrix(camera.combined());
        batch.begin();
        drawing = true;
    }

    @Override
    public void endRender() {
        if (drawing) {
            batch.end();
            drawing = false;
        }
    }

    /**
     * Sends everything queued so far to the GPU. Call before drawing with another batch (such as Scene2D), whose
     * shader would otherwise be bound when this batch is finally flushed.
     */
    public void flush() {
        if (drawing) {
            batch.flush();
        }
    }

    public void draw(TextureRegion region, float x, float y, float width, float height) {
        requireDrawing();
        batch.draw(region, x, y, width, height);
    }

    public void draw(TextureRegion region, float x, float y, float originX, float originY,
                     float width, float height, float scaleX, float scaleY, float rotationDegrees) {
        requireDrawing();
        batch.draw(region, x, y, originX, originY, width, height, scaleX, scaleY, rotationDegrees);
    }

    /** Draws a sprite descriptor, centered on its position, honoring scale, rotation, flip and tint. */
    public void draw(Sprite2D sprite) {
        requireDrawing();
        batch.setColor(sprite.red, sprite.green, sprite.blue, sprite.alpha);
        float scaleX = sprite.flipX ? -sprite.scaleX : sprite.scaleX;
        float scaleY = sprite.flipY ? -sprite.scaleY : sprite.scaleY;
        float halfWidth = sprite.width / 2f;
        float halfHeight = sprite.height / 2f;
        batch.draw(sprite.region, sprite.x - halfWidth, sprite.y - halfHeight, halfWidth, halfHeight,
                sprite.width, sprite.height, scaleX, scaleY, sprite.rotationDegrees);
        batch.setColor(Color.WHITE);
    }

    public void drawAll(List<Sprite2D> sprites) {
        for (int i = 0; i < sprites.size(); i++) {
            draw(sprites.get(i));
        }
    }

    /** Fills a rectangle (world coordinates, lower-left corner) with a solid color. */
    public void fillRect(float x, float y, float width, float height, Color color) {
        requireDrawing();
        batch.setColor(color);
        batch.draw(whitePixel, x, y, width, height);
        batch.setColor(Color.WHITE);
    }

    /** Covers everything the camera currently sees with a solid color (used by fades). */
    public void fillScreen(Color color) {
        float width = viewport.getWorldWidth() * camera.zoom();
        float height = viewport.getWorldHeight() * camera.zoom();
        fillRect(camera.x() - width, camera.y() - height, width * 2f, height * 2f, color);
    }
    public GameCamera camera() {
        return camera;
    }

    public CoordinateConverter coordinates() {
        return coordinates;
    }

    public RenderContext renderContext() {
        return renderContext;
    }

    public float worldWidth() {
        return config.worldWidth();
    }

    public float worldHeight() {
        return config.worldHeight();
    }

    @Override
    public void dispose() {
        batch.dispose();
        whitePixelTexture.dispose();
    }

    private void requireDrawing() {
        if (!drawing) {
            throw new IllegalStateException("Drawing is only allowed during the render pass");
        }
    }
}
