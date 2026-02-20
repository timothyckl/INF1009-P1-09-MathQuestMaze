package com.p1_7.demo.display;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.p1_7.abstractengine.entity.Entity;
import com.p1_7.abstractengine.render.ICustomRenderable;
import com.p1_7.abstractengine.render.IRenderItem;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.demo.Settings;
import com.p1_7.demo.core.Transform2D;

/**
 * interactive volume slider for adjusting music volume.
 *
 * responds to LEFT/RIGHT arrow keys to adjust volume in 0.1 increments.
 * renders as a horizontal bar with filled portion representing current volume.
 * updates Settings.MUSIC_VOLUME when changed.
 */
public class VolumeSlider extends Entity implements IRenderItem, ICustomRenderable {

    /** 2d spatial transform */
    private final Transform2D transform;

    /** slider width in pixels */
    private final float width;

    /** slider height in pixels */
    private final float height;

    /** current volume value (0.0 to 1.0) */
    private float value;

    /**
     * constructs a volume slider at the specified position.
     *
     * @param x the x position (left edge)
     * @param y the y position (bottom edge)
     * @param width the slider width in pixels
     */
    public VolumeSlider(float x, float y, float width) {
        super();
        this.width = width;
        this.height = 20f;
        this.transform = new Transform2D(x, y, width, height);
        this.value = Settings.MUSIC_VOLUME;
    }

    /**
     * updates the slider state based on keyboard input.
     * adjusts volume when LEFT/RIGHT arrow keys are pressed.
     *
     * @param deltaTime time elapsed since last update
     */
    public void update(float deltaTime) {
        // decrease volume with left arrow
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            value = Math.max(0f, value - 0.1f);
            Settings.setMusicVolume(value);
        }

        // increase volume with right arrow
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            value = Math.min(1f, value + 0.1f);
            Settings.setMusicVolume(value);
        }
    }

    /**
     * returns the current volume value.
     *
     * @return the volume level (0.0 to 1.0)
     */
    public float getValue() {
        return value;
    }

    /**
     * returns the slider width.
     *
     * @return the width in pixels
     */
    public float getWidth() {
        return width;
    }

    /**
     * returns the slider height.
     *
     * @return the height in pixels
     */
    public float getHeight() {
        return height;
    }

    /**
     * renders the volume slider as a grey bar with green fill representing current volume.
     *
     * @param batch the sprite batch (not used)
     * @param shapeRenderer the shape renderer (currently active)
     */
    @Override
    public void renderCustom(SpriteBatch batch, ShapeRenderer shapeRenderer) {
        float[] position = transform.getPosition();
        float x = position[0];
        float y = position[1];

        // draw background bar (grey)
        shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1f);
        shapeRenderer.rect(x, y, width, height);

        // draw filled portion (green)
        shapeRenderer.setColor(0.2f, 0.8f, 0.2f, 1f);
        shapeRenderer.rect(x, y, width * value, height);
    }

    @Override
    public String getAssetPath() {
        // null signals procedural rendering (shapes, not texture)
        return null;
    }

    @Override
    public ITransform getTransform() {
        return transform;
    }
}
