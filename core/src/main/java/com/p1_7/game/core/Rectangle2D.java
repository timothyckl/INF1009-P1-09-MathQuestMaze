package com.p1_7.game.core;

import com.p1_7.abstractengine.collision.IBounds;

/**
 * concrete 2D axis-aligned rectangular bounds for the game package.
 *
 * manages a bounding box defined by minimum position (x, y) and
 * extent (width, height). uses the separating axis theorem for
 * axis-aligned rectangle overlap detection.
 */
public class Rectangle2D implements IBounds {

    // bottom-left corner (x, y)
    private float[] minPosition = new float[2];

    // width, height
    private float[] extent = new float[2];

    public Rectangle2D(float x, float y, float width, float height) {
        this.minPosition[0] = x;
        this.minPosition[1] = y;
        this.extent[0] = width;
        this.extent[1] = height;
    }

    /** default constructor — zero-sized at origin, updated via set(). */
    public Rectangle2D() {
        this(0f, 0f, 0f, 0f);
    }

    @Override
    public boolean overlaps(IBounds other) {
        if (!(other instanceof Rectangle2D)) return false;

        Rectangle2D rect = (Rectangle2D) other;

        float x  = minPosition[0], y  = minPosition[1];
        float w  = extent[0],      h  = extent[1];
        float rx = rect.minPosition[0], ry = rect.minPosition[1];
        float rw = rect.extent[0],      rh = rect.extent[1];

        return x < rx + rw && x + w > rx && y < ry + rh && y + h > ry;
    }

    @Override
    public float[] getMinPosition() { return minPosition; }

    @Override
    public float[] getExtent() { return extent; }

    @Override
    public void set(float[] minPosition, float[] extent) {
        System.arraycopy(minPosition, 0, this.minPosition, 0, 2);
        System.arraycopy(extent,      0, this.extent,      0, 2);
    }

    /** convenience setter using individual floats. */
    public void set(float x, float y, float width, float height) {
        this.minPosition[0] = x;
        this.minPosition[1] = y;
        this.extent[0] = width;
        this.extent[1] = height;
    }

    @Override
    public int getDimensions() { return 2; }
}
