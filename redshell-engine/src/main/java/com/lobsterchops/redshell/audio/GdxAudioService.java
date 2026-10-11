package com.lobsterchops.redshell.audio;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.lobsterchops.redshell.assets.AssetService;
import com.lobsterchops.redshell.assets.catalog.AssetGroup;
import com.lobsterchops.redshell.audio.catalog.AudioCatalog;
import com.lobsterchops.redshell.audio.catalog.SoundDefinition;
import com.lobsterchops.redshell.core.context.EngineContext;
import com.lobsterchops.redshell.core.lifecycle.EngineSystem;
import com.lobsterchops.redshell.debug.log.Log;
import java.util.HashMap;
import java.util.Map;

/**
 * LibGDX-backed audio. Sounds come from the scene's asset group; music is loaded (and released) by this service
 * so it can outlive scenes. On the web, playback waits for the first key press or click (browser autoplay policy).
 */
public final class GdxAudioService implements AudioService, EngineSystem {

    private static final String TAG = "Audio";

    private final AudioCatalog catalog;
    private final AssetService assets;
    private final AudioMixer mixer = new AudioMixer();
    private final Map<String, AssetGroup> musicGroups = new HashMap<>();
    private Log log;
    private boolean locked;

    private Track current;
    private Track outgoing;
    private String pendingMusic;
    private float pendingFade;

    public GdxAudioService(AudioCatalog catalog, AssetService assets) {
        this.catalog = catalog;
        this.assets = assets;
    }

    @Override
    public void initialize(EngineContext context) {
        log = context.log();
        locked = Gdx.app != null && Gdx.app.getType() == Application.ApplicationType.WebGL;
    }

    @Override
    public AudioMixer mixer() {
        return mixer;
    }

    @Override
    public void playSound(String soundId) {
        if (locked) {
            return;
        }
        SoundDefinition definition = catalog.get(soundId);
        if (!assets.isLoaded(definition.assetId())) {
            warn("Sound '" + soundId + "' is not loaded; add its asset to the scene's asset group");
            return;
        }
        Sound sound = assets.get(definition.assetId(), Sound.class);
        sound.play(mixer.effectiveVolume(definition.category(), definition.volume()));
    }

    @Override
    public void playMusic(String musicId, float fadeSeconds) {
        SoundDefinition definition = catalog.get(musicId);
        if (current != null && current.definition == definition) {
            pendingMusic = null;
            return;
        }
        if (locked) {
            pendingMusic = musicId;
            pendingFade = fadeSeconds;
            return;
        }
        pendingMusic = null;
        retireOutgoing();
        outgoing = current;
        if (outgoing != null) {
            outgoing.fadeTarget = 0f;
            outgoing.fadeSpeed = rate(fadeSeconds);
        }
        current = open(definition);
        if (current != null) {
            current.fade = fadeSeconds > 0f ? 0f : 1f;
            current.fadeTarget = 1f;
            current.fadeSpeed = rate(fadeSeconds);
            apply(current);
            current.music.play();
        }
    }

    @Override
    public void stopMusic(float fadeSeconds) {
        pendingMusic = null;
        retireOutgoing();
        outgoing = current;
        current = null;
        if (outgoing != null) {
            outgoing.fadeTarget = 0f;
            outgoing.fadeSpeed = rate(fadeSeconds);
        }
    }

    @Override
    public String currentMusic() {
        if (current != null) {
            return current.definition.id();
        }
        return pendingMusic;
    }

    @Override
    public void update(float deltaTime) {
        if (locked && Gdx.input != null && (Gdx.input.justTouched() || Gdx.input.isKeyPressed(Input.Keys.ANY_KEY))) {
            locked = false;
            if (pendingMusic != null) {
                playMusic(pendingMusic, pendingFade);
            }
        }
        step(current, deltaTime);
        step(outgoing, deltaTime);
        if (outgoing != null && outgoing.fade <= 0f) {
            retireOutgoing();
        }
    }

    @Override
    public void dispose() {
        retireOutgoing();
        if (current != null) {
            close(current);
            current = null;
        }
    }

    private void step(Track track, float deltaTime) {
        if (track == null) {
            return;
        }
        if (track.fade < track.fadeTarget) {
            track.fade = Math.min(track.fadeTarget, track.fade + track.fadeSpeed * deltaTime);
        } else if (track.fade > track.fadeTarget) {
            track.fade = Math.max(track.fadeTarget, track.fade - track.fadeSpeed * deltaTime);
        }
        apply(track);
    }

    private void apply(Track track) {
        track.music.setVolume(mixer.effectiveVolume(track.definition.category(), track.definition.volume()) * track.fade);
    }

    private static float rate(float fadeSeconds) {
        return fadeSeconds > 0f ? 1f / fadeSeconds : 1000f;
    }

    private Track open(SoundDefinition definition) {
        AssetGroup group = musicGroups.get(definition.assetId());
        if (group == null) {
            group = assets.catalog().defineGroup("audio.music." + definition.assetId(), definition.assetId());
            musicGroups.put(definition.assetId(), group);
        }
        assets.load(group);
        assets.finishLoading();
        Music music = assets.get(definition.assetId(), Music.class);
        music.setLooping(true);
        return new Track(definition, music, group);
    }

    private void close(Track track) {
        track.music.stop();
        assets.unload(track.group);
    }

    private void retireOutgoing() {
        if (outgoing != null) {
            close(outgoing);
            outgoing = null;
        }
    }

    private void warn(String message) {
        if (log != null) {
            log.warn(TAG, message);
        }
    }

    private static final class Track {
        private final SoundDefinition definition;
        private final Music music;
        private final AssetGroup group;
        private float fade = 1f;
        private float fadeTarget = 1f;
        private float fadeSpeed = 1000f;

        Track(SoundDefinition definition, Music music, AssetGroup group) {
            this.definition = definition;
            this.music = music;
            this.group = group;
        }
    }
}