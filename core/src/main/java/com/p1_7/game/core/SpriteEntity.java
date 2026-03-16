package com.p1_7.game.core;

import com.p1_7.abstractengine.collision.IBounds;
import com.p1_7.abstractengine.collision.ICollidable;
import com.p1_7.abstractengine.entity.Entity;
import com.p1_7.abstractengine.movement.IMovable;
import com.p1_7.abstractengine.render.IRenderItem;
import com.p1_7.abstractengine.transform.ITransform;

/**
 * abstract base class for movable, collidable sprite entities in the game package.
 *
 * provides Euler-integration physics, axis-aligned bounding box collision,
 * and sprite rendering support. subclasses implement onCollision() to define
 * their collision response.
 */
public abstract class SpriteEntity extends Entity implements IRenderItem, IMovable, ICollidable {

    /** 2D spatial transform (position + size). */
    protected final Transform2D transform;

    /** path to the sprite asset; null uses the procedural render path. */
    protected final String assetPath;

    /** velocity vector [vx, vy] in pixels per second. */
    protected float[] velocity     = new float[2];

    /** acceleration vector [ax, ay] in pixels per second squared. */
    protected float[] acceleration = new float[2];

    /** cached bounding rectangle synced from transform each frame. */
    private final Rectangle2D bounds = new Rectangle2D();

    protected SpriteEntity(String assetPath, float x, float y, float width, float height) {
        super();
        this.assetPath = assetPath;
        this.transform  = new Transform2D(x, y, width, height);
    }

    // ── IRenderItem ──────────────────────────────────────────────────────────

    @Override public String     getAssetPath() { return assetPath; }
    @Override public ITransform getTransform() { return transform; }

    // ── IMovable ─────────────────────────────────────────────────────────────

    @Override public float[] getAcceleration() { return acceleration; }
    @Override public float[] getVelocity()     { return velocity; }

    @Override
    public void setAcceleration(float[] a) {
        System.arraycopy(a, 0, this.acceleration, 0, 2);
    }

    @Override
    public void setVelocity(float[] v) {
        System.arraycopy(v, 0, this.velocity, 0, 2);
    }

    @Override
    public void move(float deltaTime) {
        velocity[0] += acceleration[0] * deltaTime;
        velocity[1] += acceleration[1] * deltaTime;
        transform.setPosition(0, transform.getPosition(0) + velocity[0] * deltaTime);
        transform.setPosition(1, transform.getPosition(1) + velocity[1] * deltaTime);
    }

    // ── ICollidable ──────────────────────────────────────────────────────────

    @Override
    public IBounds getBounds() {
        bounds.set(
            transform.getPosition(0), transform.getPosition(1),
            transform.getSize(0),     transform.getSize(1));
        return bounds;
    }

    @Override
    public abstract void onCollision(ICollidable other);
}
