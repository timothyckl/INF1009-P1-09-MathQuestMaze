package com.p1_7.abstractengine.collision;

/**
 * stateless utility that checks whether two ICollidable
 * objects overlap.
 *
 * the check is performed by delegating to the IBounds.overlaps()
 * method on the bounds returned by each collidable.
 */
public class CollisionDetector {

    /**
     * determines whether the bounding volumes of the two
     * collidables overlap.
     *
     * @param a the first collidable
     * @param b the second collidable
     * @return true if their bounds overlap
     */
    public boolean checkCollision(ICollidable a, ICollidable b) {
        return a.getBounds().overlaps(b.getBounds());
    }
}
