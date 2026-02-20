package com.p1_7.abstractengine.scene;

import com.p1_7.abstractengine.entity.IEntityManager;
import com.p1_7.abstractengine.input.IInputQuery;
import com.p1_7.abstractengine.render.IRenderQueue;

/**
 * snapshot of engine state passed into every Scene callback.
 *
 * a Scene receives a SceneContext so that it can query and mutate
 * entities, submit render items, read input, and request scene
 * transitions — without holding direct references to the underlying
 * managers.
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
     * requests a deferred transition to the scene identified by key.
     * the transition is resolved at the top of the next update tick.
     *
     * @param key the name of the target scene
     */
    void changeScene(String key);

    /**
     * requests a deferred suspend transition to the scene identified by key.
     * unlike changeScene(), this preserves the current scene's state by calling
     * onSuspend() instead of onExit().
     *
     * use this for pause menus, settings overlays, or any transition where
     * the current scene should resume exactly where it left off.
     *
     * the transition is resolved at the top of the next update tick.
     *
     * @param key the name of the target scene
     */
    void suspendScene(String key);

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
