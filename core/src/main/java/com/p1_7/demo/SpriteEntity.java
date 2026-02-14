package com.p1_7.demo;

import com.p1_7.abstractengine.collision.IBounds;
import com.p1_7.abstractengine.collision.ICollidable;
import com.p1_7.abstractengine.entity.Entity;
import com.p1_7.abstractengine.movement.IMovable;
import com.p1_7.abstractengine.render.IRenderItem;
import com.p1_7.abstractengine.transform.ITransform;

/**
 * abstract base class for sprite entities, reducing boilerplate.
 *
 * implements common behaviour for 2D entities with sprites, including
 * movement via Euler integration and collision bounds derived from
 * transform position and size.
 */
public abstract class SpriteEntity extends Entity implements IRenderItem, IMovable, ICollidable {

    /** 2d spatial transform */
    protected final Transform2D transform;

    /** path to sprite asset */
    protected final String assetPath;

    /** velocity vector [x, y] in pixels per second */
    protected float[] velocity = new float[2];

    /** acceleration vector [x, y] in pixels per second squared */
    protected float[] acceleration = new float[2];

    /** cached bounding rectangle derived from transform */
    private final Rectangle2D bounds = new Rectangle2D();

    /**
     * constructs a sprite entity with the specified asset and transform.
     *
     * @param assetPath the path to the sprite texture asset
     * @param x         the initial x position
     * @param y         the initial y position
     * @param width     the sprite width
     * @param height    the sprite height
     */
    protected SpriteEntity(String assetPath, float x, float y, float width, float height) {
        super();
        this.assetPath = assetPath;
        this.transform = new Transform2D(x, y, width, height);
    }

    // ==================== IRenderItem ====================

    @Override
    public String getAssetPath() {
        return assetPath;
    }

    @Override
    public ITransform getTransform() {
        return transform;
    }

    // ==================== IMovable ====================

    @Override
    public float[] getAcceleration() {
        return acceleration;
    }

    @Override
    public void setAcceleration(float[] acceleration) {
        System.arraycopy(acceleration, 0, this.acceleration, 0, 2);
    }

    @Override
    public float[] getVelocity() {
        return velocity;
    }

    @Override
    public void setVelocity(float[] velocity) {
        System.arraycopy(velocity, 0, this.velocity, 0, 2);
    }

    @Override
    public void move(float deltaTime) {
        // euler integration: v += a*dt; p += v*dt
        velocity[0] += acceleration[0] * deltaTime;
        velocity[1] += acceleration[1] * deltaTime;

        float[] position = transform.getPosition();
        position[0] += velocity[0] * deltaTime;
        position[1] += velocity[1] * deltaTime;
    }

    // ==================== ICollidable ====================

    @Override
    public IBounds getBounds() {
        // derive bounding box from transform (sync cached rectangle)
        float[] position = transform.getPosition();
        float[] size = transform.getSize();
        bounds.set(position[0], position[1], size[0], size[1]);
        return bounds;
    }

    /**
     * called when this entity collides with another collidable.
     * subclasses override to implement collision response.
     *
     * @param other the collidable that this entity collided with
     */
    @Override
    public abstract void onCollision(ICollidable other);
}
