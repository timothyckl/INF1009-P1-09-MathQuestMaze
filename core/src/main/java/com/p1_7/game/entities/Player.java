package com.p1_7.game.entities;

import com.p1_7.abstractengine.collision.ICollidable;
import com.p1_7.abstractengine.input.IInputQuery;
import com.p1_7.game.core.SpriteEntity;
import com.p1_7.game.input.GameActions;

/**
 * player-controlled entity for Math Quest Maze.
 *
 * moves in four directions via keyboard input. tracks health and score.
 * on wall collision the player is pushed back to their previous position,
 * preventing movement through maze walls.
 *
 * register with MovementManager so move() is called automatically.
 * call updateMovement() from the scene's update() each frame.
 */
public class Player extends SpriteEntity {

    public static final float PLAYER_WIDTH  = 48f;
    public static final float PLAYER_HEIGHT = 48f;
    public static final float PLAYER_SPEED  = 200f;

    private int health = 3;
    private int score  = 0;

    /** position before the last move() call — used to undo wall collisions. */
    private float prevX, prevY;

    private PlayerEventHandler eventHandler;

    /**
     * @param x initial x position (bottom-left of sprite)
     * @param y initial y position (bottom-left of sprite)
     */
    public Player(float x, float y) {
        super("player.png", x, y, PLAYER_WIDTH, PLAYER_HEIGHT);
        this.prevX = x;
        this.prevY = y;
    }

    /**
     * sets the handler for player events (e.g. enemy hit).
     * call this from the scene after creating the player.
     */
    public void setEventHandler(PlayerEventHandler handler) {
        this.eventHandler = handler;
    }

    /**
     * reads input and sets velocity for this frame.
     * call from the scene's update() before MovementManager processes movement.
     *
     * diagonal movement is intentionally allowed; the scene can clamp to
     * one axis at a time if preferred.
     */
    public void updateMovement(IInputQuery input) {
        velocity[0] = 0f;
        velocity[1] = 0f;

        if (input.isActionActive(GameActions.LEFT))  velocity[0] -= PLAYER_SPEED;
        if (input.isActionActive(GameActions.RIGHT)) velocity[0] += PLAYER_SPEED;
        if (input.isActionActive(GameActions.DOWN))  velocity[1] -= PLAYER_SPEED;
        if (input.isActionActive(GameActions.UP))    velocity[1] += PLAYER_SPEED;
    }

    /**
     * overrides move() to snapshot position before Euler integration.
     * this snapshot is used in onCollision(Wall) to undo the move.
     */
    @Override
    public void move(float deltaTime) {
        prevX = transform.getPosition(0);
        prevY = transform.getPosition(1);
        super.move(deltaTime);
    }

    @Override
    public void onCollision(ICollidable other) {
        if (other instanceof Wall) {
            // undo the move so the player cannot pass through walls
            transform.setPosition(0, prevX);
            transform.setPosition(1, prevY);
            velocity[0] = 0f;
            velocity[1] = 0f;
        } else if (other instanceof Enemy) {
            if (eventHandler != null) eventHandler.onEnemyHit();
            // invincibility-frame logic should be handled in the scene's
            // PlayerEventHandler implementation to avoid repeated damage
        }
        // AnswerTile and ExitDoor call their own handlers via their onCollision
    }

    // ── health & score ───────────────────────────────────────────────────────

    public int  getHealth()            { return health; }
    public int  getScore()             { return score; }
    public void setHealth(int health)  { this.health = health; }
    public void setScore(int score)    { this.score = score; }
    public void addScore(int points)   { this.score += points; }
    public void loseHealth()           { this.health = Math.max(0, this.health - 1); }
    public boolean isAlive()           { return this.health > 0; }
}
