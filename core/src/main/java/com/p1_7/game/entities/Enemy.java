package com.p1_7.game.entities;

import com.p1_7.abstractengine.collision.ICollidable;
import com.p1_7.game.core.SpriteEntity;

/**
 * patrol enemy for Math Quest Maze.
 *
 * walks horizontally between two x-axis waypoints at a fixed speed,
 * reversing direction when it reaches either end or hits a wall.
 *
 * register with MovementManager so move() is called automatically.
 * call updateAI() from the scene's update() each frame so the
 * direction check runs before movement is applied next frame.
 */
public class Enemy extends SpriteEntity {

    public static final float ENEMY_WIDTH  = 48f;
    public static final float ENEMY_HEIGHT = 48f;
    public static final float ENEMY_SPEED  = 100f;

    /** left boundary of the patrol range (x coordinate). */
    private final float patrolMinX;

    /** right boundary of the patrol range (x coordinate). */
    private final float patrolMaxX;

    /**
     * @param x          initial x position (bottom-left of sprite)
     * @param y          initial y position (bottom-left of sprite)
     * @param patrolMinX left x boundary for the patrol route
     * @param patrolMaxX right x boundary for the patrol route
     */
    public Enemy(float x, float y, float patrolMinX, float patrolMaxX) {
        super("enemy.png", x, y, ENEMY_WIDTH, ENEMY_HEIGHT);
        this.patrolMinX = patrolMinX;
        this.patrolMaxX = patrolMaxX;
        this.velocity[0] = ENEMY_SPEED; // start moving right
    }

    /**
     * checks patrol boundaries and flips horizontal direction if needed.
     * call from the scene's update() each frame.
     */
    public void updateAI() {
        float x = transform.getPosition(0);
        if (x >= patrolMaxX) velocity[0] = -ENEMY_SPEED;
        if (x <= patrolMinX) velocity[0]  =  ENEMY_SPEED;
    }

    @Override
    public void onCollision(ICollidable other) {
        if (other instanceof Wall) {
            // bounce off walls in case the patrol range overlaps one
            velocity[0] = -velocity[0];
        }
        // Player handles damage in its own onCollision
    }
}
