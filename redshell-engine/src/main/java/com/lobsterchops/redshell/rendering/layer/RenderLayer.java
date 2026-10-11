package com.lobsterchops.redshell.rendering.layer;

/** A named draw layer. Lower {@link #order()} draws first (behind). Games may define their own. */
public final class RenderLayer {

    public static final RenderLayer BACKGROUND = new RenderLayer("BACKGROUND", 0);
    public static final RenderLayer ACTORS = new RenderLayer("ACTORS", 100);
    public static final RenderLayer FOREGROUND = new RenderLayer("FOREGROUND", 200);
    public static final RenderLayer UI = new RenderLayer("UI", 300);

    private final String name;
    private final int order;

    public RenderLayer(String name, int order) {
        this.name = name;
        this.order = order;
    }

    public String name() {
        return name;
    }

    public int order() {
        return order;
    }

    @Override
    public String toString() {
        return name;
    }
}