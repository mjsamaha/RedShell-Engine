package com.lobsterchops.redshell.core.config;

import com.lobsterchops.redshell.debug.log.LogLevel;

/**
 * Immutable global engine switches. Fixed timestep is opt-in per game; simple games use plain delta time.
 * Create instances with {@link #builder()} or use {@link #defaults()}.
 */
public final class EngineConfig {

    public static final String REGISTRY_NAME = "engine";

    private final boolean fixedTimestepEnabled;
    private final float fixedStepSeconds;
    private final float maxFrameSeconds;
    private final boolean debugEnabled;
    private final LogLevel logLevel;

    private EngineConfig(Builder builder) {
        this.fixedTimestepEnabled = builder.fixedTimestepEnabled;
        this.fixedStepSeconds = builder.fixedStepSeconds;
        this.maxFrameSeconds = builder.maxFrameSeconds;
        this.debugEnabled = builder.debugEnabled;
        this.logLevel = builder.logLevel;
    }

    public static EngineConfig defaults() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean fixedTimestepEnabled() {
        return fixedTimestepEnabled;
    }

    /** Length of one fixed simulation step. */
    public float fixedStepSeconds() {
        return fixedStepSeconds;
    }

    /** Upper bound for a single frame's delta and for accumulated fixed-step time, protecting against stalls. */
    public float maxFrameSeconds() {
        return maxFrameSeconds;
    }

    public boolean debugEnabled() {
        return debugEnabled;
    }

    public LogLevel logLevel() {
        return logLevel;
    }

    public static final class Builder {
        private boolean fixedTimestepEnabled = false;
        private float fixedStepSeconds = 1f / 60f;
        private float maxFrameSeconds = 0.25f;
        private boolean debugEnabled = false;
        private LogLevel logLevel = LogLevel.INFO;

        private Builder() {
        }

        public Builder fixedTimestep(boolean enabled) {
            this.fixedTimestepEnabled = enabled;
            return this;
        }

        public Builder fixedStepSeconds(float seconds) {
            this.fixedStepSeconds = seconds;
            return this;
        }

        public Builder maxFrameSeconds(float seconds) {
            this.maxFrameSeconds = seconds;
            return this;
        }

        public Builder debug(boolean enabled) {
            this.debugEnabled = enabled;
            return this;
        }

        public Builder logLevel(LogLevel level) {
            this.logLevel = level;
            return this;
        }

        public EngineConfig build() {
            if (fixedStepSeconds <= 0f) {
                throw new IllegalArgumentException("fixedStepSeconds must be positive: " + fixedStepSeconds);
            }
            if (maxFrameSeconds < fixedStepSeconds) {
                throw new IllegalArgumentException(
                        "maxFrameSeconds must be at least fixedStepSeconds: " + maxFrameSeconds);
            }
            if (logLevel == null) {
                throw new IllegalArgumentException("logLevel must not be null");
            }
            return new EngineConfig(this);
        }
    }
}
