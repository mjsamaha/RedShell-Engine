package com.lobsterchops.redshell.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.lobsterchops.redshell.core.context.EngineContext;
import com.lobsterchops.redshell.core.lifecycle.EngineSystem;
import java.util.ArrayList;
import java.util.List;

/**
 * Owns the single {@link InputMultiplexer} installed in LibGDX. Event-driven consumers (such as UI stages)
 * register here with a priority; higher priority processors see events first.
 */
public final class InputRouter implements EngineSystem {

    private final InputMultiplexer multiplexer = new InputMultiplexer();
    private final List<Entry> entries = new ArrayList<>();

    @Override
    public void initialize(EngineContext context) {
        Gdx.input.setInputProcessor(multiplexer);
    }

    public void add(InputProcessor processor, int priority) {
        entries.add(new Entry(processor, priority));
        rebuild();
    }

    public void remove(InputProcessor processor) {
        for (int i = entries.size() - 1; i >= 0; i--) {
            if (entries.get(i).processor == processor) {
                entries.remove(i);
            }
        }
        rebuild();
    }

    public InputMultiplexer multiplexer() {
        return multiplexer;
    }

    @Override
    public void dispose() {
        if (Gdx.input != null && Gdx.input.getInputProcessor() == multiplexer) {
            Gdx.input.setInputProcessor(null);
        }
        entries.clear();
        multiplexer.clear();
    }

    private void rebuild() {
        List<Entry> sorted = new ArrayList<>(entries);
        // Stable insertion sort by descending priority (GWT-safe, tiny lists).
        for (int i = 1; i < sorted.size(); i++) {
            Entry current = sorted.get(i);
            int j = i - 1;
            while (j >= 0 && sorted.get(j).priority < current.priority) {
                sorted.set(j + 1, sorted.get(j));
                j--;
            }
            sorted.set(j + 1, current);
        }
        multiplexer.clear();
        for (int i = 0; i < sorted.size(); i++) {
            multiplexer.addProcessor(sorted.get(i).processor);
        }
    }

    private static final class Entry {
        private final InputProcessor processor;
        private final int priority;

        Entry(InputProcessor processor, int priority) {
            this.processor = processor;
            this.priority = priority;
        }
    }
}