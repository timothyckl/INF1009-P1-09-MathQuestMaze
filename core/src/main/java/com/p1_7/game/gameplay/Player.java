package com.p1_7.game.gameplay;

import com.badlogic.gdx.graphics.Color;
import com.p1_7.abstractengine.collision.IBounds;
import com.p1_7.abstractengine.collision.ICollidable;
import com.p1_7.abstractengine.entity.Entity;
import com.p1_7.abstractengine.input.IInputQuery;
import com.p1_7.abstractengine.movement.IMovable;
import com.p1_7.abstractengine.render.IDrawContext;
import com.p1_7.abstractengine.render.IRenderable;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.core.Bounds2D;
import com.p1_7.game.core.Transform2D;
import com.p1_7.game.input.GameActions;
import com.p1_7.game.platform.GdxDrawContext;

/**
 * the player entity — a cyan square that moves freely around the maze.
 *
 * movement is locked during non-interactive phases (QUESTION_INTRO, FEEDBACK,
 * ROUND_RESET). wall collision is handled reactively by MazeCollisionManager,
 * which pushes the player out of any penetrating wall after move() runs.
 *
 * call update() each frame to resolve input and set velocity. position
 * integration is delegated to GameMovementManager, which calls move() after
 * GameScene.update() has set the velocity. MazeCollisionManager.onUpdate()
 * runs after GameMovementManager and corrects any wall penetration.
 */
public class Player extends Entity implements IRenderable, IMovable, ICollidable {

    /** movement speed in pixels per second */
    private static final float SPEED = 200f;

    /** player width and height in pixels */
    private static final float SIZE = 32f;

    /** bottom-left origin, SIZE × SIZE */
    private final Transform2D transform;

    /** AABB synced with transform position on each getBounds() call */
    private final Bounds2D bounds;

    /** reusable position scratch array for getBounds() — avoids per-call allocation */
    private final float[] boundsPos = new float[2];

    /** constant extent array shared across all getBounds() calls */
    private static final float[] BOUNDS_SIZE = new float[]{ SIZE, SIZE };

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
        this.bounds       = new Bounds2D(spawnX - SIZE / 2f, spawnY - SIZE / 2f, SIZE, SIZE);
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
     * called by GameMovementManager each frame after GameScene.update() has set
     * the velocity. wall penetration is corrected reactively by MazeCollisionManager
     * after this call returns.
     *
     * @param deltaTime seconds elapsed since the previous frame
     */
    @Override
    public void move(float deltaTime) {
        transform.setPosition(0, transform.getPosition(0) + velocity[0] * deltaTime);
        transform.setPosition(1, transform.getPosition(1) + velocity[1] * deltaTime);
    }

    // ── ICollidable ──────────────────────────────────────────────

    /**
     * returns the player's bounding box, synced to the current transform position.
     *
     * @return the AABB for this frame's position
     */
    @Override
    public IBounds getBounds() {
        // sync bounds to the current transform position using pre-allocated scratch arrays
        boundsPos[0] = transform.getPosition(0);
        boundsPos[1] = transform.getPosition(1);
        bounds.set(boundsPos, BOUNDS_SIZE);
        return bounds;
    }

    /**
     * no-op — position correction is handled by MazeCollisionManager.resolve().
     *
     * @param other the collidable that this player collided with
     */
    @Override
    public void onCollision(ICollidable other) {
        // position correction delegated to MazeCollisionManager
    }

    // ── per-frame update ─────────────────────────────────────────

    /**
     * resolves input and applies phase locking for this frame.
     *
     * must be called before move(). the resolved velocity is committed via
     * setVelocity(). wall collision is handled reactively by MazeCollisionManager
     * after move() runs — no inline AABB check is needed here.
     *
     * @param deltaTime  seconds elapsed since the previous frame
     * @param inputQuery the logical input query for this frame
     * @param phase      the current round phase; movement is locked unless CHOOSING
     */
    public void update(float deltaTime, IInputQuery inputQuery, RoundPhase phase) {
        // only allow movement during the choosing phase; any other phase locks the player
        if (phase != RoundPhase.CHOOSING) {
            setVelocity(new float[]{ 0f, 0f });
            return;
        }

        // read raw directional input and commit as velocity
        float rawVx = (inputQuery.isActionActive(GameActions.MOVE_RIGHT) ? SPEED : 0f)
                    - (inputQuery.isActionActive(GameActions.MOVE_LEFT)  ? SPEED : 0f);
        float rawVy = (inputQuery.isActionActive(GameActions.MOVE_UP)    ? SPEED : 0f)
                    - (inputQuery.isActionActive(GameActions.MOVE_DOWN)  ? SPEED : 0f);

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

}
