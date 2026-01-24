package com.p1_7.abstractengine.properties;

import com.p1_7.abstractengine.core.AbstractProperty;

public class Position extends AbstractProperty {
    float x;
    float y;

    // default constructor initialises position to (0, 0) on a 2D plane
    public Position() {
        this.x = 0f;
        this.y = 0f;
    }

    public Position(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
