package com.lobsterchops.redshell.debug.log;

import java.util.ArrayList;
import java.util.List;

/** Keeps the most recent log lines in memory, so tools such as the debug overlay can show them. Works on every platform. */
public final class MemoryLogSink implements LogSink {

    private final String[] ring;
    private int next;
    private int count;

    public MemoryLogSink(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive: " + capacity);
        }
        this.ring = new String[capacity];
    }

    @Override
    public void write(LogLevel level, String tag, String message, Throwable throwable) {
        String line = level + " [" + tag + "] " + message;
        if (throwable != null) {
            line += " (" + throwable + ")";
        }
        ring[next] = line;
        next = (next + 1) % ring.length;
        if (count < ring.length) {
            count++;
        }
    }

    public int size() {
        return count;
    }

    /** The newest {@code max} lines, oldest first. */
    public List<String> recent(int max) {
        int n = Math.min(max, count);
        List<String> lines = new ArrayList<>(n);
        for (int i = n; i >= 1; i--) {
            int index = ((next - i) % ring.length + ring.length) % ring.length;
            lines.add(ring[index]);
        }
        return lines;
    }

    public void clear() {
        next = 0;
        count = 0;
    }
}