package com.p1_7.game;
import com.p1_7.abstractengine.input.InputMapping;
import com.p1_7.game.input.MappableActions;

/**
 * Game application configuration values.
 *
 * All fields are public static so that any game class can read or
 * override them at startup. The default values mirror those set in
 * Lwjgl3Launcher; if the launcher changes the window size the game
 * should update these fields accordingly.
 */
public class Settings {

    /** width of the application window in pixels */
    public static int WINDOW_WIDTH = 1280;

    /** height of the application window in pixels */
    public static int WINDOW_HEIGHT = 720;

    /** music volume level (0.0 = silent, 1.0 = maximum) */
    public static float VOLUME_LEVEL = 0.5f; // default 50% volume

    public static float BRIGHTNESS_LEVEL = 1.0f; // default 100%

    /**
     * Sets the music volume with validation.
     * Clamps the value between 0.0 (silent) and 1.0 (maximum).
     *
     * @param volume the desired volume level
     */
    public static void setMusicVolume(float volume) {
        VOLUME_LEVEL = Math.max(0f, Math.min(1f, volume));
    }

    public static void setBrightnessLevel(float brightness) {
        BRIGHTNESS_LEVEL = Math.max(0f, Math.min(1f, brightness));
    }
}
