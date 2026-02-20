package com.p1_7.abstractengine.collision;

import com.badlogic.gdx.utils.Array;

import com.p1_7.abstractengine.engine.UpdatableManager;

/**
 * abstract per-frame manager that tests all registered ICollidable entities
 * for pairwise overlap and delegates resolution to concrete subclasses.
 */
public abstract class CollisionManager extends UpdatableManager {

    /** all collidable entities managed by this manager */
    private final Array<ICollidable> collidables = new Array<>();

    /** stateless detector that performs the overlap test */
    private final CollisionDetector detector = new CollisionDetector();

    /** detected collisions from the current frame */
    private final Array<CollisionPair> detectedCollisions = new Array<>();

    /**
     * adds an ICollidable to the detection list.
     *
     * @param collidable the collidable entity to register
     */
    public void registerCollidable(ICollidable collidable) {
        collidables.add(collidable);
    }

    /**
     * removes an ICollidable from the detection list.
     *
     * @param collidable the collidable entity to unregister
     */
    public void unregisterCollidable(ICollidable collidable) {
        collidables.removeValue(collidable, true);
    }

    /**
     * runs collision detection and resolution in two phases:
     * first detects all collisions, then resolves them.
     *
     * @param deltaTime seconds elapsed since the previous frame
     */
    @Override
    protected void onUpdate(float deltaTime) {
        detect();
        resolve(detectedCollisions);
    }

    /**
     * detects all collisions for the current frame by iterating unique pairs.
     */
    protected void detect() {
        detectedCollisions.clear();
        for (int i = 0; i < collidables.size - 1; i++) {
            ICollidable a = collidables.get(i);
            for (int j = i + 1; j < collidables.size; j++) {
                ICollidable b = collidables.get(j);
                if (detector.checkCollision(a, b)) {
                    detectedCollisions.add(new CollisionPair(a, b));
                }
            }
        }
    }

    /**
     * resolves detected collisions for the current frame.
     *
     * @param collisions the array of detected collision pairs from this frame
     */
    protected abstract void resolve(Array<CollisionPair> collisions);
}
