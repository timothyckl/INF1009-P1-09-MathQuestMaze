package com.p1_7.abstractengine.render;

/**
 * base capability interface for any object the render system can draw.
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
