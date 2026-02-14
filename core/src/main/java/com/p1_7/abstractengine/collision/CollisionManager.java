package com.p1_7.abstractengine.collision;

import com.badlogic.gdx.utils.Array;

import com.p1_7.abstractengine.engine.UpdatableManager;

/**
 * abstract per-frame manager that tests all registered ICollidable
 * entities for pairwise overlap using a two-phase architecture:
 * detection followed by resolution.
 *
 * entities must be explicitly registered via
 * registerCollidable(ICollidable). the detection phase uses a
 * stateless CollisionDetector and iterates unique pairs (O(n²))
 * to identify all collisions in the current frame. the resolution phase
 * processes these detected collisions according to the strategy implemented
 * by concrete subclasses.
 *
 * subclasses can override detect() to implement optimised
 * detection algorithms (spatial partitioning, grid-based, etc.) and must
 * implement resolve(Array) to define collision resolution behaviour
 * (callbacks, physics impulses, layered filtering, etc.).
 */
public abstract class CollisionManager extends UpdatableManager {

    /** all collidable entities managed by this manager */
    private final Array<ICollidable> collidables = new Array<>();

    /** stateless detector that performs the overlap test */
    private final CollisionDetector detector = new CollisionDetector();

    /** detected collisions from the current frame */
    private final Array<CollisionPair> detectedCollisions = new Array<>();

    // ---------------------------------------------------------------
    // registration
    // ---------------------------------------------------------------

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

    // ---------------------------------------------------------------
    // UpdatableManager hook
    // ---------------------------------------------------------------

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

    // ---------------------------------------------------------------
    // detection & resolution
    // ---------------------------------------------------------------

    /**
     * detects all collisions by iterating unique pairs (i, j) where
     * i < j. detected collisions are stored in detectedCollisions
     * for processing by resolve(Array).
     *
     * this method clears the previous frame's collisions before detection.
     * subclasses can override this method to implement optimised detection
     * algorithms (spatial partitioning, quadtrees, etc.).
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
     * resolves detected collisions according to the collision resolution
     * strategy implemented by concrete subclasses.
     *
     * examples of resolution strategies include:
     * 1. callback-based: invoke ICollidable.onCollision(ICollidable) on both entities
     * 2. physics-based: apply impulses or forces to separate entities
     * 3. layered: filter collisions based on entity groups or categories
     * 4. batched: group collisions by type and handle differently
     *
     * @param collisions the array of detected collision pairs from this frame
     */
    protected abstract void resolve(Array<CollisionPair> collisions);
}
