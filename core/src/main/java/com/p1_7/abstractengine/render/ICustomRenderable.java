package com.p1_7.abstractengine.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * interface for entities that provide their own rendering logic.
 */
public interface ICustomRenderable {

    /**
     * renders this entity using custom logic.
     *
     * @param batch the sprite batch (currently inactive)
     * @param shapeRenderer the shape renderer (currently active)
     */
    void renderCustom(SpriteBatch batch, ShapeRenderer shapeRenderer);
}
