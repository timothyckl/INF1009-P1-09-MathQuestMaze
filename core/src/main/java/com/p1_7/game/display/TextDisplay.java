package com.p1_7.game.display;

/**
 * reusable text rendering entity for displaying arbitrary text.
 *
 * extends basetextdisplay to leverage shared text rendering logic.
 * font scale can be adjusted at construction time to create
 * titles, subtitles, or body text.
 */
public class TextDisplay extends BaseTextDisplay {

    /** current text content */
    private String text;

    /**
     * constructs a text display with the specified text, position, and scale.
     *
     * @param text the text to display
     * @param x the x position (left edge)
     * @param y the y position (baseline)
     * @param scale the font scale multiplier (1.0f = normal size)
     */
    public TextDisplay(String text, float x, float y, float scale) {
        // call baseclass constructor with 0x0 size (not used for text)
        super(x, y, 0f, 0f, scale);
        this.text = text;
    }

    /**
     * sets the text content to display.
     *
     * @param text the new text value
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * returns the current text content.
     *
     * @return the text string
     */
    @Override
    public String getText() {
        return text;
    }
}
