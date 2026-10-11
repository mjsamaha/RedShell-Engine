package com.lobsterchops.redshell.audio;

import static org.junit.jupiter.api.Assertions.*;

import com.lobsterchops.redshell.audio.catalog.AudioCatalog;
import org.junit.jupiter.api.Test;

class AudioTest {

    @Test
    void effectiveVolumeMultipliesLevels() {
        AudioMixer m = new AudioMixer();
        m.setMasterVolume(0.5f);
        m.setCategoryVolume(AudioCategory.SFX, 0.5f);
        assertEquals(0.125f, m.effectiveVolume(AudioCategory.SFX, 0.5f), 1e-5);
    }

    @Test
    void muteSilencesEverything() {
        AudioMixer m = new AudioMixer();
        m.setMuted(true);
        assertEquals(0f, m.effectiveVolume(AudioCategory.MUSIC, 1f), 1e-6);
    }

    @Test
    void volumesAreClamped() {
        AudioMixer m = new AudioMixer();
        m.setMasterVolume(5f);
        assertEquals(1f, m.masterVolume(), 1e-6);
        m.setMasterVolume(-1f);
        assertEquals(0f, m.masterVolume(), 1e-6);
    }

    @Test
    void catalogRejectsDuplicates() {
        AudioCatalog c = new AudioCatalog().sound("a", "asset.a", AudioCategory.SFX, 1f);
        assertTrue(c.has("a"));
        assertThrows(RuntimeException.class, () -> c.sound("a", "asset.b", AudioCategory.SFX, 1f));
    }
}