package com.lobsterchops.redshell.save.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A versioned bag of string values. Typed accessors convert to and from text. */
public final class SaveData {

    private int version;
    private final Map<String, String> values = new LinkedHashMap<>();

    public SaveData(int version) {
        this.version = version;
    }

    public int version() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean has(String key) {
        return values.containsKey(key);
    }

    public void remove(String key) {
        values.remove(key);
    }

    public List<String> keys() {
        return new ArrayList<>(values.keySet());
    }

    public void putString(String key, String value) {
        values.put(key, value);
    }

    public void putInt(String key, int value) {
        values.put(key, Integer.toString(value));
    }

    public void putFloat(String key, float value) {
        values.put(key, Float.toString(value));
    }

    public void putBoolean(String key, boolean value) {
        values.put(key, Boolean.toString(value));
    }

    public String getString(String key, String fallback) {
        String value = values.get(key);
        return value == null ? fallback : value;
    }

    public int getInt(String key, int fallback) {
        try {
            String value = values.get(key);
            return value == null ? fallback : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public float getFloat(String key, float fallback) {
        try {
            String value = values.get(key);
            return value == null ? fallback : Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public boolean getBoolean(String key, boolean fallback) {
        String value = values.get(key);
        return value == null ? fallback : Boolean.parseBoolean(value);
    }

    /** Text form: first line {@code version=N}, then one {@code key=value} per line, values escaped. */
    public String serialize() {
        StringBuilder out = new StringBuilder("version=").append(version);
        for (Map.Entry<String, String> entry : values.entrySet()) {
            out.append('\n').append(entry.getKey()).append('=').append(escape(entry.getValue()));
        }
        return out.toString();
    }

    public static SaveData parse(String text) {
        String[] lines = text.split("\n");
        if (lines.length == 0 || !lines[0].startsWith("version=")) {
            throw new IllegalArgumentException("Save data has no version header");
        }
        SaveData data;
        try {
            data = new SaveData(Integer.parseInt(lines[0].substring("version=".length()).trim()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Save data has an invalid version header");
        }
        for (int i = 1; i < lines.length; i++) {
            int equals = lines[i].indexOf('=');
            if (equals <= 0) {
                continue;
            }
            data.values.put(lines[i].substring(0, equals), unescape(lines[i].substring(equals + 1)));
        }
        return data;
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\n", "\\n");
    }

    private static String unescape(String value) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\\' && i + 1 < value.length()) {
                char next = value.charAt(++i);
                out.append(next == 'n' ? '\n' : next);
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}