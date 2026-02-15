package com.p1_7.demo;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.p1_7.abstractengine.entity.Entity;
import com.p1_7.abstractengine.render.IRenderItem;
import com.p1_7.abstractengine.transform.ITransform;

/**
 * text entity displaying current score.
 *
 * uses libGDX BitmapFont for text rendering. returns null
 * from getAssetPath() to signal to RenderManager that this
 * requires text rendering instead of texture drawing.
 */
public class ScoreDisplay extends Entity implements IRenderItem {

    /** 2d spatial transform */
    private final Transform2D transform;

    /** font used for rendering the score text */
    private final BitmapFont font;

    /** current score */
    private int score;

    /**
     * constructs a score display with the specified initial score and position.
     *
     * @param x the x position (left edge)
     * @param y the y position (baseline)
     * @param initialScore the starting score
     */
    public ScoreDisplay(float x, float y, int initialScore) {
        super();
        this.score = initialScore;
        this.font = new BitmapFont(); // libgdx default font
        this.transform = new Transform2D(x, y, 100f, 20f);
    }

    /**
     * sets the current score.
     *
     * @param score the new score value
     */
    public void setScore(int score) {
        this.score = score;
    }

    /**
     * returns the current score.
     *
     * @return the score value
     */
    public int getScore() {
        return score;
    }

    /**
     * returns the formatted text to display.
     *
     * @return the score text string
     */
    public String getText() {
        return "Score: " + score;
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
