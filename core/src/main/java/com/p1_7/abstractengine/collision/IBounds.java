package com.p1_7.abstractengine.collision;

/**
 * dimension-agnostic collision boundary for an entity.
 *
 * all spatial data is expressed as plain float[] arrays. the length
 * of each array is determined by the concrete implementation; the
 * abstract engine does not assume any particular dimensionality.
 * demo code that targets 2-D will use arrays of length 2.
 *
 * implementations define both the shape representation and the
 * overlap detection logic. examples include axis-aligned rectangles,
 * circles, polygons, and 3D bounding volumes.
 */
public interface IBounds {

    /**
     * determines whether this bounds overlaps with another bounds.
     *
     * implementations must handle checking against different bounds
     * types. for example, a Rectangle2D must detect overlap with
     * both other Rectangle2D instances and potentially Circle2D
     * instances.
     *
     * @param other the bounds to check against; must not be null
     * @return true if the bounds overlap, false otherwise
     */
    boolean overlaps(IBounds other);

    /**
     * returns the minimum corner position as a float array.
     *
     * for 2D axis-aligned rectangles, this is the bottom-left corner
     * (x, y). for 3D axis-aligned boxes, this is the minimum corner
     * (x, y, z).
     *
     * @return the minimum position vector; length equals the number of dimensions
     */
    float[] getMinPosition();

    /**
     * returns the extent (size) in each dimension as a float array.
     *
     * for 2D rectangles, this is (width, height). for 3D boxes, this
     * is (width, height, depth).
     *
     * @return the size vector; length equals the number of dimensions
     */
    float[] getExtent();

    /**
     * updates the bounds to the specified minimum position and extent.
     *
     * this method is used to sync cached bounds instances with entity
     * transforms without creating new objects each frame.
     *
     * @param minPosition the new minimum corner position
     * @param extent the new size in each dimension
     */
    void set(float[] minPosition, float[] extent);

    /**
     * returns the number of spatial dimensions this bounds operates in.
     *
     * concrete implementations decide the value; demo code will typically
     * return 2.
     *
     * @return the dimensionality (length of position and extent arrays)
     */
    int getDimensions();
}
