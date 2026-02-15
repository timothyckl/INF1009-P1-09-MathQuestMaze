package com.p1_7.demo;

/**
 * demo application configuration values.
 *
 * all fields are public static so that any demo class can read or
 * override them at startup. the default values mirror those set in
 * Lwjgl3Launcher; if the launcher changes the window size the demo
 * should update these fields accordingly.
 */
public class Settings {

    /** width of the application window in pixels */
    public static int WINDOW_WIDTH = 640;

    /** height of the application window in pixels */
    public static int WINDOW_HEIGHT = 480;

    /** music volume level (0.0 = silent, 1.0 = maximum) */
    public static float MUSIC_VOLUME = 0.5f; // default 50% volume

    /**
     * sets the music volume with validation.
     * clamps the value between 0.0 (silent) and 1.0 (maximum).
     *
     * @param volume the desired volume level
     */
    public static void setMusicVolume(float volume) {
        MUSIC_VOLUME = Math.max(0f, Math.min(1f, volume));
    }
}
