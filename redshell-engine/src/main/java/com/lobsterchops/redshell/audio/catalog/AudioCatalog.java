package com.lobsterchops.redshell.audio.catalog;

import com.lobsterchops.redshell.audio.AudioCategory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/** Registry of every sound and music track by logical ID. Declarative only; it loads nothing. */
public final class AudioCatalog {

    private final Map<String, SoundDefinition> definitions = new LinkedHashMap<>();

    public AudioCatalog sound(String id, String assetId, AudioCategory category, float volume) {
        return add(new SoundDefinition(id, assetId, category, volume, false));
    }

    public AudioCatalog music(String id, String assetId, float volume) {
        return add(new SoundDefinition(id, assetId, AudioCategory.MUSIC, volume, true));
    }

    public boolean has(String id) {
        return definitions.containsKey(id);
    }

    public SoundDefinition get(String id) {
        SoundDefinition definition = definitions.get(id);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown audio id: " + id);
        }
        return definition;
    }

    public Collection<SoundDefinition> all() {
        return new ArrayList<>(definitions.values());
    }

    private AudioCatalog add(SoundDefinition definition) {
        if (definition.id() == null || definition.assetId() == null) {
            throw new IllegalArgumentException("id and assetId are required");
        }
        if (definitions.containsKey(definition.id())) {
            throw new IllegalArgumentException("Duplicate audio id: " + definition.id());
        }
        definitions.put(definition.id(), definition);
        return this;
    }
}