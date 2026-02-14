package com.p1_7.demo;

import com.p1_7.abstractengine.collision.IBounds;

/**
 * concrete 2D axis-aligned rectangular bounds implementation.
 *
 * manages a bounding box defined by minimum position (x, y) and
 * extent (width, height). implements overlap detection using the
 * separating axis theorem for axis-aligned rectangles.
 *
 * this implementation assumes positive width and height values.
 */
public class Rectangle2D implements IBounds {

    // bottom-left corner (x, y)
    private float[] minPosition = new float[2];

    // width, height
    private float[] extent = new float[2];

    /**
     * constructs a 2D rectangular bounds with the specified position and size.
     *
     * @param x      the x coordinate of the bottom-left corner
     * @param y      the y coordinate of the bottom-left corner
     * @param width  the width (must be positive)
     * @param height the height (must be positive)
     */
    public Rectangle2D(float x, float y, float width, float height) {
        this.minPosition[0] = x;
        this.minPosition[1] = y;
        this.extent[0] = width;
        this.extent[1] = height;
    }

    /**
     * default constructor creating a zero-sized bounds at origin.
     * useful for creating cached instances that will be updated via set().
     */
    public Rectangle2D() {
        this(0f, 0f, 0f, 0f);
    }

    @Override
    public boolean overlaps(IBounds other) {
        // for now, only support overlapping with other Rectangle2D instances
        // future: could add Circle2D, Polygon2D support via instanceof checks
        if (!(other instanceof Rectangle2D)) {
            // conservative: assume no overlap with unknown types
            return false;
        }

        Rectangle2D rect = (Rectangle2D) other;

        // separating axis theorem for axis-aligned rectangles
        // this bounds: [x, x+width] x [y, y+height]
        // other bounds: [rx, rx+rwidth] x [ry, ry+rheight]
        // overlap if intervals overlap in both dimensions
        float x = minPosition[0];
        float y = minPosition[1];
        float width = extent[0];
        float height = extent[1];

        float rx = rect.minPosition[0];
        float ry = rect.minPosition[1];
        float rwidth = rect.extent[0];
        float rheight = rect.extent[1];

        // equivalent to libGDX Rectangle.overlaps() logic:
        // x < rx + rwidth && x + width > rx && y < ry + rheight && y + height > ry
        return x < rx + rwidth
                && x + width > rx
                && y < ry + rheight
                && y + height > ry;
    }

    @Override
    public float[] getMinPosition() {
        return minPosition;
    }

    @Override
    public float[] getExtent() {
        return extent;
    }

    @Override
    public void set(float[] minPosition, float[] extent) {
        // use System.arraycopy to prevent external mutation
        System.arraycopy(minPosition, 0, this.minPosition, 0, 2);
        System.arraycopy(extent, 0, this.extent, 0, 2);
    }

    /**
     * convenience setter using individual float values instead of arrays.
     *
     * @param x      the x coordinate of the bottom-left corner
     * @param y      the y coordinate of the bottom-left corner
     * @param width  the width
     * @param height the height
     */
    public void set(float x, float y, float width, float height) {
        this.minPosition[0] = x;
        this.minPosition[1] = y;
        this.extent[0] = width;
        this.extent[1] = height;
    }

    @Override
    public int getDimensions() {
        return 2;
    }
}
