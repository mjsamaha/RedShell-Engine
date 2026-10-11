package com.lobsterchops.redshell.debug.profiler;

import com.badlogic.gdx.utils.TimeUtils;
import com.lobsterchops.redshell.core.lifecycle.ScopeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Measures named scopes (each engine system's update and render, each world system) and keeps a rolling average
 * per scope. Call {@link #endFrame()} once per frame to roll the totals into the statistics. A scope that runs
 * several times in a frame is summed; scopes must not nest with the same name.
 */
public final class Profiler implements ScopeListener {

    private final int window;
    private final Map<String, Scope> byName = new HashMap<>();
    private final List<Scope> ordered = new ArrayList<>();

    public Profiler(int window) {
        this.window = window;
    }

    @Override
    public void begin(String scope) {
        Scope s = byName.get(scope);
        if (s == null) {
            s = new Scope(scope, new FrameStats(window));
            byName.put(scope, s);
            ordered.add(s);
        }
        s.startNanos = TimeUtils.nanoTime();
    }

    @Override
    public void end(String scope) {
        Scope s = byName.get(scope);
        if (s != null) {
            s.frameNanos += TimeUtils.nanoTime() - s.startNanos;
        }
    }

    /** Rolls this frame's scope totals into the rolling statistics and starts a new frame. */
    public void endFrame() {
        for (int i = 0; i < ordered.size(); i++) {
            Scope s = ordered.get(i);
            s.stats.record(s.frameNanos / 1_000_000f);
            s.frameNanos = 0L;
        }
    }

    /** Scopes in first-seen order. */
    public List<Scope> scopes() {
        return Collections.unmodifiableList(ordered);
    }

    public Scope scope(String name) {
        return byName.get(name);
    }

    /** One measured scope and its rolling statistics in milliseconds. */
    public static final class Scope {
        private final String name;
        private final FrameStats stats;
        private long startNanos;
        private long frameNanos;

        Scope(String name, FrameStats stats) {
            this.name = name;
            this.stats = stats;
        }

        public String name() {
            return name;
        }

        public FrameStats stats() {
            return stats;
        }
    }
}