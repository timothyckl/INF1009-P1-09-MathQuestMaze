package com.p1_7.abstractengine.core.properties;

import com.p1_7.abstractengine.core.AbstractProperty;

public class Position extends AbstractProperty {
    private float x;
    private float y;

    public Position() {
        this.x = 0f;
        this.y = 0f;
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
