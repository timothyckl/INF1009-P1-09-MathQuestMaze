package com.p1_7.abstractengine.transform;

/**
 * dimension-agnostic spatial state for an entity.
 *
 * all positional and size data is expressed as plain float[]
 * arrays. the length of each array is determined by the concrete
 * implementation; the abstract engine does not assume any particular
 * dimensionality. demo code that targets 2-D will use arrays of
 * length 2.
 */
public interface ITransform {

    /**
     * returns the current position as a float array.
     *
     * @return the position vector; length equals the number of dimensions
     */
    float[] getPosition();

    /**
     * sets the position to the supplied values.
     *
     * @param position the new position vector
     */
    void setPosition(float[] position);

    /**
     * returns the current size (extent in each dimension) as a float array.
     *
     * @return the size vector; length equals the number of dimensions
     */
    float[] getSize();

    /**
     * sets the size to the supplied values.
     *
     * @param size the new size vector
     */
    void setSize(float[] size);

    /**
     * returns the number of spatial dimensions this transform operates in.
     * concrete implementations decide the value; demo code will typically
     * return 2.
     *
     * @return the dimensionality (length of the position and size arrays)
     */
    int getDimensions();
}
