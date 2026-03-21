package com.p1_7.game.gameplay;

import com.badlogic.gdx.graphics.Color;
import com.p1_7.abstractengine.entity.Entity;
import com.p1_7.abstractengine.input.IInputQuery;
import com.p1_7.abstractengine.movement.IMovable;
import com.p1_7.abstractengine.render.IDrawContext;
import com.p1_7.abstractengine.render.IRenderable;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.core.Transform2D;
import com.p1_7.game.input.GameActions;
import com.p1_7.game.platform.GdxDrawContext;

/**
 * the player entity — a cyan square that moves freely around the maze.
 *
 * movement is locked during non-interactive phases (QUESTION_INTRO, FEEDBACK,
 * ROUND_RESET). the player is blocked by the outer walls from MazeLayout via
 * an inline axis-separated AABB check, which will be removed in issue #112 once
 * MazeCollisionManager is live.
 *
 * call update() each frame to resolve input and wall collisions, then move() to
 * integrate velocity into position. in issue #112, GameScene will stop calling
 * move() directly and register this entity with MovementManager instead.
 */
public class Player extends Entity implements IRenderable, IMovable {

    /** movement speed in pixels per second */
    private static final float SPEED = 200f;

    /** player width and height in pixels */
    private static final float SIZE = 32f;

    /** bottom-left origin, SIZE × SIZE */
    private final Transform2D transform;

    /** [vx, vy] — set each frame by update() */
    private float[] velocity;

    /** [ax, ay] — unused for now, satisfies IMovable */
    private float[] acceleration;

    /**
     * constructs a player centred on the given spawn coordinates.
     *
     * @param spawnX world x coordinate of the desired spawn centre
     * @param spawnY world y coordinate of the desired spawn centre
     */
    public Player(float spawnX, float spawnY) {
        // position transform so the player is centred on the spawn point
        this.transform    = new Transform2D(spawnX - SIZE / 2f, spawnY - SIZE / 2f, SIZE, SIZE);
        this.velocity     = new float[]{ 0f, 0f };
        this.acceleration = new float[]{ 0f, 0f };
    }

    // ── ITransformable ───────────────────────────────────────────

    @Override
    public ITransform getTransform() {
        return transform;
    }

    // ── IRenderable ──────────────────────────────────────────────

    /**
     * returns null — this entity is shape-rendered; no texture asset required.
     *
     * @return null
     */
    @Override
    public String getAssetPath() {
        return null;
    }

    /**
     * draws the player as a solid cyan rectangle.
     *
     * @param ctx the draw context for this frame
     */
    @Override
    public void render(IDrawContext ctx) {
        GdxDrawContext gdx = (GdxDrawContext) ctx;
        gdx.rect(Color.CYAN,
                 transform.getPosition(0),
                 transform.getPosition(1),
                 SIZE, SIZE, true);
    }

    // ── IMovable ─────────────────────────────────────────────────

    /**
     * returns a defensive copy of the current velocity vector.
     *
     * @return a new [vx, vy] array
     */
    @Override
    public float[] getVelocity() {
        return velocity.clone();
    }

    /**
     * replaces the velocity vector with a defensive copy of the supplied array.
     *
     * @param velocity the new [vx, vy] values
     */
    @Override
    public void setVelocity(float[] velocity) {
        this.velocity = velocity.clone();
    }

    /**
     * returns a defensive copy of the current acceleration vector.
     *
     * @return a new [ax, ay] array
     */
    @Override
    public float[] getAcceleration() {
        return acceleration.clone();
    }

    /**
     * replaces the acceleration vector with a defensive copy of the supplied array.
     *
     * @param accel the new [ax, ay] values
     */
    @Override
    public void setAcceleration(float[] accel) {
        this.acceleration = accel.clone();
    }

    /**
     * integrates the current velocity into the player's position.
     *
     * wall collision is pre-resolved by update() before this is called, so no
     * further bounds checks are needed here.
     *
     * @param deltaTime seconds elapsed since the previous frame
     */
    @Override
    public void move(float deltaTime) {
        transform.setPosition(0, transform.getPosition(0) + velocity[0] * deltaTime);
        transform.setPosition(1, transform.getPosition(1) + velocity[1] * deltaTime);
    }

    // ── per-frame update ─────────────────────────────────────────

    /**
     * resolves input, applies phase locking, and performs an axis-separated AABB
     * wall check against the maze layout for this frame.
     *
     * must be called before move(). the resolved velocity is committed via
     * setVelocity() so MovementManager can read it in issue #112.
     *
     * @param deltaTime  seconds elapsed since the previous frame
     * @param inputQuery the logical input query for this frame
     * @param phase      the current round phase; movement is locked unless CHOOSING
     * @param layout     the maze layout supplying wall bounds
     */
    public void update(float deltaTime, IInputQuery inputQuery, RoundPhase phase,
                       MazeLayout layout) {
        // only allow movement during the choosing phase; any other phase locks the player
        if (phase != RoundPhase.CHOOSING) {
            setVelocity(new float[]{ 0f, 0f });
            return;
        }

        // read raw directional input
        float rawVx = (inputQuery.isActionActive(GameActions.MOVE_RIGHT) ? SPEED : 0f)
                    - (inputQuery.isActionActive(GameActions.MOVE_LEFT)  ? SPEED : 0f);
        float rawVy = (inputQuery.isActionActive(GameActions.MOVE_UP)    ? SPEED : 0f)
                    - (inputQuery.isActionActive(GameActions.MOVE_DOWN)  ? SPEED : 0f);

        float x = transform.getPosition(0);
        float y = transform.getPosition(1);

        // two-pass axis-separated AABB wall check — each axis is resolved independently so
        // one wall zeroing x cannot influence the y test for a different wall in the same frame
        for (float[] wall : layout.getWallBounds()) {
            if (overlaps(x + rawVx * deltaTime, y, SIZE, SIZE,
                         wall[0], wall[1], wall[2], wall[3])) {
                rawVx = 0f;
                break;
            }
        }
        for (float[] wall : layout.getWallBounds()) {
            if (overlaps(x, y + rawVy * deltaTime, SIZE, SIZE,
                         wall[0], wall[1], wall[2], wall[3])) {
                rawVy = 0f;
                break;
            }
        }

        setVelocity(new float[]{ rawVx, rawVy });
    }

    /**
     * recentres the player on the given spawn coordinates.
     *
     * @param spawnPoint a two-element [x, y] array for the new spawn centre; must not be null
     * @throws IllegalArgumentException if spawnPoint is null or not length 2
     */
    public void resetToSpawn(float[] spawnPoint) {
        if (spawnPoint == null) {
            throw new IllegalArgumentException("spawnPoint must not be null");
        }
        if (spawnPoint.length != 2) {
            throw new IllegalArgumentException(
                "spawnPoint must have exactly 2 elements, got: " + spawnPoint.length);
        }

        transform.setPosition(0, spawnPoint[0] - SIZE / 2f);
        transform.setPosition(1, spawnPoint[1] - SIZE / 2f);
    }

    // ── private helpers ──────────────────────────────────────────

    /**
     * returns true if rectangle A overlaps rectangle B.
     *
     * @param ax left edge of A
     * @param ay bottom edge of A
     * @param aw width of A
     * @param ah height of A
     * @param bx left edge of B
     * @param by bottom edge of B
     * @param bw width of B
     * @param bh height of B
     * @return true when the two axis-aligned bounding boxes intersect
     */
    private static boolean overlaps(float ax, float ay, float aw, float ah,
                                     float bx, float by, float bw, float bh) {
        return ax < bx + bw && ax + aw > bx
            && ay < by + bh && ay + ah > by;
    }
}
