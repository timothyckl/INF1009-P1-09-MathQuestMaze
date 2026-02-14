package com.p1_7.demo;

import com.badlogic.gdx.utils.Array;

import com.p1_7.abstractengine.collision.CollisionManager;
import com.p1_7.abstractengine.collision.CollisionPair;

/**
 * demo-specific collision manager that resolves collisions by invoking
 * callback methods on both entities.
 *
 * this implementation provides the simple callback-based resolution
 * strategy used in the demo application. each detected collision results
 * in both entities receiving their {@link com.p1_7.abstractengine.collision.ICollidable#onCollision}
 * callback with a reference to the other entity.
 *
 * this is the simplest collision resolution strategy and is suitable
 * for applications where entities manage their own collision response logic.
 * more complex applications may implement different resolution strategies
 * such as physics-based impulses or layered collision filtering.
 */
public class DemoCollisionManager extends CollisionManager {

    /**
     * resolves collisions by invoking {@code onCollision()} callbacks
     * on both entities in each collision pair.
     *
     * @param collisions the array of detected collision pairs from this frame
     */
    @Override
    protected void resolve(Array<CollisionPair> collisions) {
        for (CollisionPair pair : collisions) {
            pair.getEntityA().onCollision(pair.getEntityB());
            pair.getEntityB().onCollision(pair.getEntityA());
        }
    }
}
