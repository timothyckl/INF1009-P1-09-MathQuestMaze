package com.p1_7.demo;

import com.p1_7.abstractengine.transform.ITransform;

/**
 * concrete 2D implementation of ITransform.
 *
 * manages position (x, y) and size (width, height) in 2D space.
 * arrays are copied on set operations to prevent external mutation.
 */
public class Transform2D implements ITransform {

    // x, y coordinates
    private float[] position = new float[2];

    // width, height dimensions
    private float[] size = new float[2];

    /**
     * constructs a 2D transform with the specified position and size.
     *
     * @param x      the x coordinate
     * @param y      the y coordinate
     * @param width  the width
     * @param height the height
     */
    public Transform2D(float x, float y, float width, float height) {
        this.position[0] = x;
        this.position[1] = y;
        this.size[0] = width;
        this.size[1] = height;
    }

    @Override
    public float[] getPosition() {
        return position;
    }

    @Override
    public void setPosition(float[] position) {
        // use System.arraycopy to prevent external mutation
        System.arraycopy(position, 0, this.position, 0, 2);
    }

    @Override
    public float[] getSize() {
        return size;
    }

    @Override
    public void setSize(float[] size) {
        // use System.arraycopy to prevent external mutation
        System.arraycopy(size, 0, this.size, 0, 2);
    }

    @Override
    public int getDimensions() {
        return 2;
    }
}
