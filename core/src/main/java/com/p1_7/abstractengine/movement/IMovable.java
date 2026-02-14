package com.p1_7.abstractengine.movement;

/**
 * capability interface for any entity that can move under the
 * influence of acceleration and velocity.
 *
 * all vectors are plain float[] arrays whose length matches
 * the dimensionality of the owning entity's
 * com.p1_7.abstractengine.transform.ITransform. the abstract
 * engine does not mandate 2-D; concrete implementations decide the
 * array length.
 */
public interface IMovable {

    /**
     * returns the current acceleration vector.
     *
     * @return the acceleration as a float array
     */
    float[] getAcceleration();

    /**
     * sets the acceleration to the supplied values.
     *
     * @param acceleration the new acceleration vector
     */
    void setAcceleration(float[] acceleration);

    /**
     * returns the current velocity vector.
     *
     * @return the velocity as a float array
     */
    float[] getVelocity();

    /**
     * sets the velocity to the supplied values.
     *
     * @param velocity the new velocity vector
     */
    void setVelocity(float[] velocity);

    /**
     * advances the entity by one physics step. concrete
     * implementations apply acceleration to velocity, then velocity
     * to position.
     *
     * @param deltaTime seconds elapsed since the previous frame
     */
    void move(float deltaTime);
}
