package com.p1_7.abstractengine.core.properties;

import com.p1_7.abstractengine.core.AbstractProperty;

public class Dimension extends AbstractProperty {
    private float width;
    private float height;

    public Dimension() {
        this.width = 1f;
        this.height = 1f;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public void setDimension(float width, float height) {
        this.width = width;
        this.height = height;
    }
}
