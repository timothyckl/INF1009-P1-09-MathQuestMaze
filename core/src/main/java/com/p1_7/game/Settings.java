package com.p1_7.game;

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
    public static float musicVolume = 0.5f;
}
