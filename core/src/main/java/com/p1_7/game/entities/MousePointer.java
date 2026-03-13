package com.p1_7.game.entities;

import com.p1_7.abstractengine.collision.IBounds;
import com.p1_7.abstractengine.collision.ICollidable;
import com.p1_7.abstractengine.entity.Entity;

/*
MousePointer is an invisible Entity that follows the mouse cursor to allow our Collision System to work better
*/
public class MousePointer extends Entity implements ICollidable {
    private final SimpleBounds bounds = new SimpleBounds(0, 0, 1, 1);

    public void updatePosition(float x, float y) {
        bounds.set(x, y, 1, 1);
    }

    @Override
    public IBounds getBounds() {
        return bounds;
    }

    @Override
    public void onCollision(ICollidable other) {
        // The mouse pointer doesn't currently have any Collision logic, since UI elements handle that themselves.
    }

    public static class SimpleBounds implements IBounds {
        private final float[] minPos = new float[2];
        private final float[] extent = new float[2];

        public SimpleBounds(float x, float y, float w, float h) {
            set(x, y, w, h);
        }

        public void set(float x, float y, float w, float h) {
            minPos[0] = x; minPos[1] = y;
            extent[0] = w; extent[1] = h;
        }

        @Override
        public void set(float[] minPosition, float[] extent) {
            System.arraycopy(minPosition, 0, this.minPos, 0, 2);
            System.arraycopy(extent, 0, this.extent, 0, 2);
        }

        @Override public float[] getMinPosition() { return minPos; }
        @Override public float[] getExtent() { return extent; }
        @Override public int getDimensions() { return 2; }

        @Override
        public boolean overlaps(IBounds other) {
            float x = minPos[0], y = minPos[1];
            float w = extent[0], h = extent[1];
            float rx = other.getMinPosition()[0], ry = other.getMinPosition()[1];
            float rw = other.getExtent()[0], rh = other.getExtent()[1];

            return x < rx + rw && x + w > rx && y < ry + rh && y + h > ry;
        }
    }
}