package com.lobsterchops.redshell.audio;

/** Plays sounds and music by logical ID. Game code never touches LibGDX {@code Sound} or {@code Music}. */
public interface AudioService {

    /** Plays a sound effect once. Its asset must be loaded (put it in the scene's asset group). */
    void playSound(String soundId);

    /** Plays a music track, crossfading from the current one. Loads and releases the asset itself. */
    void playMusic(String musicId, float fadeSeconds);

    void stopMusic(float fadeSeconds);

    /** ID of the music that is playing or waiting for the browser to allow audio, or null. */
    String currentMusic();

    AudioMixer mixer();
}