package com.p1_7.abstractengine.render;

/**
 * base capability interface for any object the render system can draw.
 *
 * if getAssetPath() returns null the entity is
 * drawn procedurally (e.g. as a filled rectangle via
 * ShapeRenderer). a non-null path indicates a textured
 * asset that should be loaded and drawn via a SpriteBatch.
 */
public interface IRenderable {

    /**
     * returns the asset path for a textured sprite, or null
     * if the entity should be drawn procedurally.
     *
     * @return the asset path string, or null
     */
    String getAssetPath();
}
