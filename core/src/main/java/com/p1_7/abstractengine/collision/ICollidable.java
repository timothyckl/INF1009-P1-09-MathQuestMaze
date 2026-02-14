package com.p1_7.abstractengine.collision;

/**
 * capability interface for any entity that participates in collision
 * detection.
 *
 * the bounding volume returned by getBounds() is used by
 * CollisionDetector to perform overlap tests. the
 * onCollision(ICollidable) callback is invoked by the
 * CollisionManager when an overlap is detected with another
 * collidable.
 */
public interface ICollidable {

    /**
     * returns the collision bounds that represent this entity's shape.
     *
     * implementations may return axis-aligned rectangles, circles,
     * polygons, or other shapes depending on their dimensionality
     * and collision requirements.
     *
     * @return the bounding volume; must not be null
     */
    IBounds getBounds();

    /**
     * called when this entity has been found to overlap with another.
     *
     * @param other the collidable that this entity collided with
     */
    void onCollision(ICollidable other);
}
