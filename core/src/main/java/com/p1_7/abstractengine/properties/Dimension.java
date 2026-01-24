package com.p1_7.abstractengine.properties;

import com.p1_7.abstractengine.core.AbstractProperty;

public class Dimension extends AbstractProperty {
    float width;
    float height;

    // default constructor initialises width and height to 1 unit.
    public Dimension() {
        this.width = 1f;
        this.height = 1f;
    }

    public Dimension(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setWidth(float width, float scale) {
        this.width = width * scale;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setHeight(float height, float scale) {
        this.height = height * scale;
    }
}
