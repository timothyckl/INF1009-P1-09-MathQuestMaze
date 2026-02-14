package com.p1_7.demo;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.p1_7.abstractengine.entity.Entity;
import com.p1_7.abstractengine.render.IRenderItem;
import com.p1_7.abstractengine.transform.ITransform;

/**
 * text entity displaying remaining lives count.
 *
 * uses libGDX BitmapFont for text rendering. returns null
 * from getAssetPath() to signal to RenderManager that this
 * requires text rendering instead of texture drawing.
 */
public class LivesDisplay extends Entity implements IRenderItem {

    /** 2d spatial transform */
    private final Transform2D transform;

    /** font used for rendering the lives text */
    private final BitmapFont font;

    /** current lives count */
    private int lives;

    /**
     * constructs a lives display with the specified initial lives.
     *
     * @param initialLives the starting number of lives
     */
    public LivesDisplay(int initialLives) {
        super();
        this.lives = initialLives;
        this.font = new BitmapFont(); // libgdx default font

        // position at top-left corner
        float x = 10f;
        float y = Settings.WINDOW_HEIGHT - 10f;
        this.transform = new Transform2D(x, y, 100f, 20f);
    }

    /**
     * sets the current lives count.
     *
     * @param lives the new lives value
     */
    public void setLives(int lives) {
        this.lives = lives;
    }

    /**
     * returns the current lives count.
     *
     * @return the number of lives remaining
     */
    public int getLives() {
        return lives;
    }

    /**
     * returns the formatted text to display.
     *
     * @return the lives text string
     */
    public String getText() {
        return "Lives: " + lives;
    }

    /**
     * returns the font used for rendering.
     *
     * @return the BitmapFont instance
     */
    public BitmapFont getFont() {
        return font;
    }

    /**
     * disposes the font resource.
     * should be called when the scene exits.
     */
    public void dispose() {
        font.dispose();
    }

    @Override
    public String getAssetPath() {
        // null signals text rendering instead of texture
        return null;
    }

    @Override
    public ITransform getTransform() {
        return transform;
    }
}
