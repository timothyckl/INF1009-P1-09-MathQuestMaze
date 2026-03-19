package com.p1_7.game.managers;

import com.badlogic.gdx.graphics.g2d.BitmapFont;

/**
 * Contract for shared game font access.
 */
public interface IFontManager {

    BitmapFont getGoldDisplayFont(int size);

    BitmapFont getDarkTextFont(int size);

    BitmapFont getPromptFont();
}
