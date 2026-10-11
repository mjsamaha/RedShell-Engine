package com.lobsterchops.redshell.ui.theme;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Fonts, colours and widget styles. Built in code from a bitmap font so no skin file is needed and it works on
 * both desktop and web. The theme owns a 1x1 texture used for every solid-colour drawable.
 */
public final class UiTheme {

    public static final Color PANEL = new Color(0f, 0f, 0f, 0.6f);
    public static final Color ACCENT = new Color(0.85f, 0.2f, 0.2f, 1f);

    private final BitmapFont font;
    private final Texture pixel;
    private final TextureRegion pixelRegion;
    private final Label.LabelStyle labelStyle;
    private final Label.LabelStyle titleStyle;
    private final TextButton.TextButtonStyle buttonStyle;
    private final Slider.SliderStyle sliderStyle;
    private final ProgressBar.ProgressBarStyle barStyle;

    /** The font is owned by the asset service; the theme only borrows it. */
    public UiTheme(BitmapFont font) {
        this.font = font;
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixel = new Texture(pixmap);
        pixmap.dispose();
        pixelRegion = new TextureRegion(pixel);

        labelStyle = new Label.LabelStyle(font, Color.WHITE);
        titleStyle = new Label.LabelStyle(font, new Color(1f, 0.85f, 0.4f, 1f));

        buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.up = solid(new Color(0.2f, 0.2f, 0.28f, 0.95f), 0f, 0f);
        buttonStyle.over = solid(new Color(0.32f, 0.32f, 0.45f, 0.95f), 0f, 0f);
        buttonStyle.down = solid(ACCENT, 0f, 0f);

        sliderStyle = new Slider.SliderStyle();
        sliderStyle.background = solid(new Color(0.15f, 0.15f, 0.2f, 1f), 0f, 6f);
        sliderStyle.knobBefore = solid(ACCENT, 0f, 6f);
        sliderStyle.knob = solid(Color.WHITE, 10f, 18f);

        barStyle = new ProgressBar.ProgressBarStyle();
        barStyle.background = solid(new Color(0.15f, 0.15f, 0.2f, 0.9f), 0f, 8f);
        barStyle.knobBefore = solid(ACCENT, 0f, 8f);
    }

    public BitmapFont font() {
        return font;
    }

    public Label.LabelStyle labelStyle() {
        return labelStyle;
    }

    public Label.LabelStyle titleStyle() {
        return titleStyle;
    }

    public TextButton.TextButtonStyle buttonStyle() {
        return buttonStyle;
    }

    public Slider.SliderStyle sliderStyle() {
        return sliderStyle;
    }

    public ProgressBar.ProgressBarStyle barStyle() {
        return barStyle;
    }

    /** A solid-colour drawable; width/height of 0 leave that dimension flexible. */
    public Drawable solid(Color color, float minWidth, float minHeight) {
        BaseDrawable drawable = (BaseDrawable) new TextureRegionDrawable(pixelRegion).tint(color);
        drawable.setMinSize(minWidth, minHeight);
        return drawable;
    }

    public void dispose() {
        pixel.dispose();
    }
}