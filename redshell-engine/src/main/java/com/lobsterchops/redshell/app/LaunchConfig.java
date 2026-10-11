package com.lobsterchops.redshell.app;

/** Immutable window and launch settings read by the desktop and web launchers. */
public final class LaunchConfig {

    public static final String REGISTRY_NAME = "launch";

    private final String title;
    private final int windowWidth;
    private final int windowHeight;
    private final boolean resizable;
    private final boolean fullscreen;
    private final boolean vsync;
    private final int targetFps;

    private LaunchConfig(Builder builder) {
        this.title = builder.title;
        this.windowWidth = builder.windowWidth;
        this.windowHeight = builder.windowHeight;
        this.resizable = builder.resizable;
        this.fullscreen = builder.fullscreen;
        this.vsync = builder.vsync;
        this.targetFps = builder.targetFps;
    }

    public static LaunchConfig defaults() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String title() {
        return title;
    }

    /** Initial window width on desktop; canvas width on web when not resizable. */
    public int windowWidth() {
        return windowWidth;
    }

    public int windowHeight() {
        return windowHeight;
    }

    /** On web, a resizable game fills the browser window. */
    public boolean resizable() {
        return resizable;
    }

    /** Ignored on web. */
    public boolean fullscreen() {
        return fullscreen;
    }

    /** Ignored on web, where the browser owns the frame loop. */
    public boolean vsync() {
        return vsync;
    }

    /** Foreground frame cap on desktop; 0 matches the monitor refresh rate. Ignored on web. */
    public int targetFps() {
        return targetFps;
    }

    public static final class Builder {
        private String title = "RedShell";
        private int windowWidth = 1280;
        private int windowHeight = 720;
        private boolean resizable = true;
        private boolean fullscreen = false;
        private boolean vsync = true;
        private int targetFps = 0;

        private Builder() {
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder windowSize(int width, int height) {
            this.windowWidth = width;
            this.windowHeight = height;
            return this;
        }

        public Builder resizable(boolean resizable) {
            this.resizable = resizable;
            return this;
        }

        public Builder fullscreen(boolean fullscreen) {
            this.fullscreen = fullscreen;
            return this;
        }

        public Builder vsync(boolean vsync) {
            this.vsync = vsync;
            return this;
        }

        public Builder targetFps(int targetFps) {
            this.targetFps = targetFps;
            return this;
        }

        public LaunchConfig build() {
            if (title == null || title.isEmpty()) {
                throw new IllegalArgumentException("title must not be empty");
            }
            if (windowWidth <= 0 || windowHeight <= 0) {
                throw new IllegalArgumentException("window size must be positive: " + windowWidth + "x" + windowHeight);
            }
            if (targetFps < 0) {
                throw new IllegalArgumentException("targetFps must not be negative: " + targetFps);
            }
            return new LaunchConfig(this);
        }
    }
}
