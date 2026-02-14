package com.p1_7.abstractengine.transform;

/**
 * marker contract for any object that carries spatial state via an
 * ITransform.
 *
 * managers such as MovementManager cast registered objects
 * to this interface when they need to read or write position and size
 * data.
 */
public interface ITransformable {

    /**
     * returns the spatial transform attached to this object.
     *
     * @return the transform; must not be null
     */
    ITransform getTransform();
}
