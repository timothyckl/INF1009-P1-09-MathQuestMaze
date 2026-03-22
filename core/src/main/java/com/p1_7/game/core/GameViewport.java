package com.p1_7.game.core;

/**
 * Fixed viewport dimensions for the game scene.
 *
 * The top of the screen is reserved for HUD-only content, leaving the lower
 * portion as the playable maze area.
 */
public final class GameViewport {

    /** full screen width in pixels */
    public static final float SCREEN_WIDTH = 1280f;

    /** full screen height in pixels */
    public static final float SCREEN_HEIGHT = 720f;

    /** reserved height for the HUD strip */
    public static final float HUD_STRIP_HEIGHT = 48f;

    /** total height available for gameplay below the HUD strip */
    public static final float PLAYFIELD_HEIGHT = SCREEN_HEIGHT - HUD_STRIP_HEIGHT;

    /** y coordinate where the HUD strip begins */
    public static final float HUD_STRIP_Y = PLAYFIELD_HEIGHT;

    private GameViewport() {
    }
}
