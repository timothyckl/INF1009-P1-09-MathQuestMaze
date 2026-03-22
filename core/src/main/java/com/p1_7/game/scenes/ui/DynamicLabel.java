package com.p1_7.game.scenes.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;

/**
 * Mutable centred text label whose content can be updated at runtime.
 *
 * Extends StaticLabel with a single setText mutator, making the mutability
 * contract explicit at the type level.
 */
public class DynamicLabel extends StaticLabel {

    /**
     * constructs a dynamic label centred at the given coordinates.
     *
     * @param text    the initial text to display
     * @param centreX horizontal centre position in screen coordinates
     * @param centreY vertical centre position in screen coordinates
     * @param font    bitmap font used to render the text
     */
    public DynamicLabel(String text, float centreX, float centreY, BitmapFont font) {
        super(text, centreX, centreY, font);
    }

    /**
     * replaces the displayed text with the given string.
     *
     * @param newText the replacement text; must not be null
     */
    public void setText(String newText) {
        this.text = newText;
    }
}
