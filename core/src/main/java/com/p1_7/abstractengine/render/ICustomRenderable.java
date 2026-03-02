package com.p1_7.abstractengine.render;

/**
 * interface for entities that provide their own rendering logic.
 */
public interface ICustomRenderable {

    /**
     * renders this entity using custom logic.
     *
     * @param batch         the sprite batch
     * @param shapeRenderer the shape renderer
     */
    void renderCustom(ISpriteBatch batch, IShapeRenderer shapeRenderer);
}
