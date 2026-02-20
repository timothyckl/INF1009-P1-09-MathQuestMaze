package com.p1_7.demo.entities;

import com.p1_7.abstractengine.collision.IBounds;
import com.p1_7.abstractengine.collision.ICollidable;
import com.p1_7.demo.core.Rectangle2D;
import com.p1_7.demo.core.SpriteEntity;

/**
 * static cloud entity that deflects falling droplets.
 *
 * when droplets collide with clouds, they are gently pushed
 * horizontally away from the cloud's centre, creating a deflection
 * effect without stopping their downward fall.
 */
public class Cloud extends SpriteEntity {

    /** cloud display width in pixels (scaled down from 796px sprite) */
    public static final float CLOUD_WIDTH = 200f;

    /** cloud display height in pixels (scaled down from 250px sprite) */
    public static final float CLOUD_HEIGHT = 63f;

    /** horizontal push speed applied to droplets (pixels per second) */
    private static final float PUSH_SPEED = 120f;

    /** vertical offset - move collision box down to skip fluffy top */
    private static final float COLLISION_OFFSET_Y = 16f;

    /** collision box width (covers most of cloud width) */
    private static final float COLLISION_WIDTH = 190f;

    /** reduced collision box height (covers solid cloud body only) */
    private static final float COLLISION_HEIGHT = 45f;

    /** cached collision bounds */
    private final Rectangle2D collisionBounds = new Rectangle2D();

    /**
     * constructs a cloud at the specified position.
     *
     * @param x the x position
     * @param y the y position
     */
    public Cloud(float x, float y) {
        super("cloud.png", x, y, CLOUD_WIDTH, CLOUD_HEIGHT);
        // clouds are static - no velocity
        velocity[0] = 0f;
        velocity[1] = 0f;
    }

    @Override
    public IBounds getBounds() {
        // create collision box that matches visible cloud body (not full sprite)
        float[] position = transform.getPosition();

        // move box down to skip transparent top pixels
        float collisionY = position[1] + COLLISION_OFFSET_Y;

        // use full width but reduced height
        collisionBounds.set(
            position[0],           // x: same as sprite
            collisionY,            // y: offset down
            COLLISION_WIDTH,
            COLLISION_HEIGHT       // height: reduced (50 instead of 81)
        );

        return collisionBounds;
    }

    @Override
    public void onCollision(ICollidable other) {
        if (other instanceof Droplet) {
            Droplet droplet = (Droplet) other;

            // get cloud collision box position
            float[] cloudPos = transform.getPosition();
            float collisionY = cloudPos[1] + COLLISION_OFFSET_Y;
            float collisionCentreY = collisionY + (COLLISION_HEIGHT / 2f);

            // get droplet position and centre
            float[] dropletPos = droplet.getTransform().getPosition();
            float dropletCentreY = dropletPos[1] + (Droplet.DROPLET_HEIGHT / 2f);
            float dropletX = dropletPos[0];

            // check if droplet is hitting from above (not from side)
            // compare vertical centres: if droplet centre is above cloud centre, it's a top hit
            boolean isTopCollision = dropletCentreY > collisionCentreY;

            // only apply horizontal push if hitting from above
            if (isTopCollision) {
                // get current velocity
                float[] velocity = droplet.getVelocity();

                // get cloud centre for push direction
                float cloudCentreX = cloudPos[0] + (CLOUD_WIDTH / 2f);

                // push horizontally while allowing slow downward movement
                // this lets droplets slide across and eventually fall out of collision box
                if (dropletX < cloudCentreX) {
                    velocity[0] = -PUSH_SPEED;  // push left
                } else {
                    velocity[0] = PUSH_SPEED;   // push right
                }
                velocity[1] = -Droplet.FALL_SPEED * 0.3f;  // slow fall while sliding

                // apply velocity
                droplet.setVelocity(velocity);
            }
            // if side collision, do nothing - let droplet fall normally past the cloud
        }
    }
}
