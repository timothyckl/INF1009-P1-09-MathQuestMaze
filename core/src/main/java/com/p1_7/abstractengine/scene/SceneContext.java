package com.p1_7.abstractengine.scene;

import com.p1_7.abstractengine.entity.IEntityRepository;
import com.p1_7.abstractengine.input.IInputQuery;
import com.p1_7.abstractengine.render.IRenderQueue;

/**
 * read-only snapshot of engine state passed into every Scene
 * callback.
 *
 * a Scene receives a SceneContext so that it can
 * query entities, submit render items and read input without holding
 * direct references to the underlying managers.
 */
public interface SceneContext {

    /**
     * returns the read-only entity repository.
     *
     * @return the IEntityRepository; never null
     */
    IEntityRepository entities();

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
     * requests a deferred transition to the scene identified by key.
     * the transition is resolved at the top of the next update tick.
     *
     * @param key the name of the target scene
     */
    void changeScene(String key);

    /**
     * returns the scene registered under the specified key.
     *
     * useful for inter-scene communication, such as passing data
     * to the target scene before transitioning.
     *
     * @param key the name of the scene to retrieve
     * @return the Scene, or null if not found
     */
    Scene getScene(String key);
}
