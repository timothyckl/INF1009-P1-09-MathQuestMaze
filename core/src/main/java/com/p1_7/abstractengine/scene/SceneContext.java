package com.p1_7.abstractengine.scene;

import com.p1_7.abstractengine.entity.IEntityManager;
import com.p1_7.abstractengine.input.IInputQuery;
import com.p1_7.abstractengine.render.IRenderQueue;

/**
 * snapshot of engine state passed into every Scene callback, providing access
 * to entities, render, input, and scene transitions.
 */
public interface SceneContext {

    /**
     * returns the entity manager, providing both read and write access
     * to the entity store.
     *
     * @return the IEntityManager; never null
     */
    IEntityManager entities();

    /**
     * returns the render queue for submitting items this frame.
     *
     * @return the IRenderQueue; never null
     */
    IRenderQueue renderQueue();

    /**
     * returns the input query interface for the current frame.
     *
     * @return the IInputQuery; never null
     */
    IInputQuery input();

    /**
     * requests a transition to the scene identified by key.
     *
     * @param key the name of the target scene
     */
    void changeScene(String key);

    /**
     * requests a deferred suspend transition, preserving the current scene's state.
     *
     * @param key the name of the target scene
     */
    void suspendScene(String key);

    /**
     * returns the scene registered under the specified key.
     *
     * @param key the name of the scene to retrieve
     * @return the Scene, or null if not found
     */
    Scene getScene(String key);
}
