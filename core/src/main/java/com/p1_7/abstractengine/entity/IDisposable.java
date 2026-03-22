package com.p1_7.abstractengine.entity;

/**
 * opt-in contract for entities that own GPU or native resources.
 * implement this interface only when the entity must release
 * resources (e.g. textures, audio buffers) on scene exit.
 *
 * entities that draw procedurally or delegate asset management
 * to the asset store do not need to implement this interface.
 */
public interface IDisposable {

    /**
     * releases all GPU and native resources owned by this entity.
     * called by the owning scene in its onExit() hook.
     */
    void dispose();
}
