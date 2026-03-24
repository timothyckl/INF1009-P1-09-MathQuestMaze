package com.p1_7.abstractengine.render;

/**
 * opt-in contract for renderable objects that own GPU or texture resources.
 * implement this interface only when the object must release render assets
 * explicitly on scene exit or when its lifecycle ends.
 *
 * renderables that delegate asset management to the asset store do not
 * need to implement this interface.
 */
public interface IDisposable {

    /**
     * releases all GPU and texture resources owned by this object.
     * called by the owner when the resources are no longer needed.
     */
    void dispose();
}
