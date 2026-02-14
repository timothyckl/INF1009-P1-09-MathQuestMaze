package com.p1_7.abstractengine.engine;

/**
 * abstract manager that participates in the per-frame update loop in
 * addition to the standard lifecycle provided by Manager.
 *
 * subclasses implement onUpdate(float) to define their
 * frame-tick behaviour. the public update(float) method is
 * final and delegates directly to that hook.
 */
public abstract class UpdatableManager extends Manager implements IUpdatable {

    /**
     * called once per frame by the engine. delegates to
     * onUpdate(float).
     *
     * @param deltaTime seconds elapsed since the previous frame
     */
    @Override
    public final void update(float deltaTime) {
        onUpdate(deltaTime);
    }

    /**
     * hook that subclasses must implement to perform their per-frame logic.
     *
     * @param deltaTime seconds elapsed since the previous frame
     */
    protected abstract void onUpdate(float deltaTime);
}
