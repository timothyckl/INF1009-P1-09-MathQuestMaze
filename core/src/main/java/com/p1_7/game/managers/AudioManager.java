package com.p1_7.game.managers;

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
            musicCache.put(key, Gdx.audio.newMusic(Gdx.files.internal(filePath)));
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
            soundCache.put(key, Gdx.audio.newSound(Gdx.files.internal(filePath)));
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
        if (key.equals(currentMusicKey)) {
            return;
        }

        if (currentMusic != null) {
            currentMusic.stop();
        }

        Music next = musicCache.get(key);
        if (next != null) {
            currentMusic = next;
            currentMusicKey = key;
            currentMusic.setLooping(loop);
            currentMusic.setVolume(Settings.MUSIC_VOLUME);
            currentMusic.play();
        }
    }

    /**
     * plays a cached sound effect once at the current music volume.
     *
     * @param key the name of the sound to play
     */
    public void playSound(String key) {
        Sound sound = soundCache.get(key);
        if (sound != null) {
            sound.play(Settings.MUSIC_VOLUME);
        }
    }

    /**
     * sets the volume on the currently playing music track.
     * call this when the player adjusts the volume setting.
     *
     * @param volume the desired volume level (0.0 = silent, 1.0 = maximum)
     */
    public void setMusicVolume(float volume) {
        if (currentMusic != null) {
            currentMusic.setVolume(volume);
        }
    }

    /**
     * disposes all loaded audio resources and clears the caches.
     */
    @Override
    protected void onShutdown() {
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
