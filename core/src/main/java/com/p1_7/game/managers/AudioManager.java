package com.p1_7.game.managers;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.p1_7.abstractengine.engine.Manager;
import com.p1_7.game.Settings;

import java.util.HashMap;
import java.util.Map;

/**
 * game-level manager for music and sound effect playback.
 *
 * integrates with the engine lifecycle via Manager; assets are loaded
 * explicitly after engine initialisation and disposed automatically on shutdown.
 * volume changes are applied directly via setMusicVolume(float) rather
 * than being polled each frame.
 */
public class AudioManager extends Manager implements IAudioManager {

    /** gdx audio backend, stored to avoid repeated static field access */
    private final Audio audio = Gdx.audio;

    /** gdx file system backend, stored to avoid repeated static field access */
    private final Files files = Gdx.files;

    /** cached music tracks, keyed by caller-supplied name */
    private final Map<String, Music> musicCache = new HashMap<>();

    /** cached sound effects, keyed by caller-supplied name */
    private final Map<String, Sound> soundCache = new HashMap<>();

    /** the currently active music track, or null if none is playing */
    private Music currentMusic;

    /** the key of the currently active music track, or null if none is playing */
    private String currentMusicKey;

    /**
     * loads a music track into the cache under the given key.
     * if the key is already cached, the call is a no-op.
     *
     * @param key      the name to associate with the track
     * @param filePath the internal asset path
     */
    public void loadMusic(String key, String filePath) {
        if (!musicCache.containsKey(key)) {
            musicCache.put(key, audio.newMusic(files.internal(filePath)));
        } else {
            Gdx.app.log("AudioManager", "loadMusic: key '" + key + "' already loaded, ignoring");
        }
    }

    /**
     * loads a sound effect into the cache under the given key.
     * if the key is already cached, the call is a no-op.
     *
     * @param key      the name to associate with the sound
     * @param filePath the internal asset path
     */
    public void loadSound(String key, String filePath) {
        if (!soundCache.containsKey(key)) {
            soundCache.put(key, audio.newSound(files.internal(filePath)));
        } else {
            Gdx.app.log("AudioManager", "loadSound: key '" + key + "' already loaded, ignoring");
        }
    }

    /**
     * plays a cached music track, stopping any currently playing track first.
     * if the requested track is already playing, the call is a no-op.
     *
     * @param key  the name of the track to play
     * @param loop whether the track should loop continuously
     */
    public void playMusic(String key, boolean loop) {
        if (key == null) {
            throw new IllegalArgumentException("music key must not be null");
        }
        if (key.equals(currentMusicKey)) {
            return;
        }

        if (currentMusic != null) {
            currentMusic.stop();
            // clear state before the cache lookup so a cache miss does not
            // leave stale references pointing at the stopped track
            currentMusic = null;
            currentMusicKey = null;
        }

        Music next = musicCache.get(key);
        if (next != null) {
            currentMusic = next;
            currentMusicKey = key;
            currentMusic.setLooping(loop);
            currentMusic.setVolume(Settings.musicVolume);
            currentMusic.play();
        } else {
            Gdx.app.log("AudioManager", "playMusic: key '" + key + "' not found in cache, ignoring");
        }
    }

    /**
     * plays a cached sound effect once at full volume.
     * a dedicated SFX volume setting is not yet implemented;
     * when one is added this method should read from it instead.
     *
     * @param key the name of the sound to play
     */
    public void playSound(String key) {
        if (key == null) {
            throw new IllegalArgumentException("sound key must not be null");
        }
        Sound sound = soundCache.get(key);
        if (sound != null) {
            sound.play(1.0f);
        } else {
            Gdx.app.log("AudioManager", "playSound: key '" + key + "' not found in cache, ignoring");
        }
    }

    /**
     * clamps the given volume, stores it in Settings.musicVolume,
     * and applies it to the currently playing music track.
     *
     * @param volume the desired volume level (0.0 = silent, 1.0 = maximum)
     */
    public void setMusicVolume(float volume) {
        Settings.musicVolume = Math.max(0f, Math.min(1f, volume));
        if (currentMusic != null) {
            currentMusic.setVolume(Settings.musicVolume);
        }
    }

    /**
     * returns the current music volume as stored in settings.
     *
     * @return the current volume level in the range [0.0, 1.0]
     */
    public float getMusicVolume() {
        return Settings.musicVolume;
    }

    /**
     * disposes all loaded audio resources and clears the caches.
     */
    @Override
    protected void onShutdown() {
        // Music.dispose() implicitly stops; no separate stop call needed
        for (Music music : musicCache.values()) {
            music.dispose();
        }
        for (Sound sound : soundCache.values()) {
            sound.dispose();
        }
        musicCache.clear();
        soundCache.clear();
        currentMusic = null;
        currentMusicKey = null;
    }
}
