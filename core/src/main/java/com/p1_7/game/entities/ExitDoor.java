package com.p1_7.game.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.p1_7.abstractengine.collision.IBounds;
import com.p1_7.abstractengine.collision.ICollidable;
import com.p1_7.abstractengine.entity.Entity;
import com.p1_7.abstractengine.render.ICustomRenderable;
import com.p1_7.abstractengine.render.IRenderItem;
import com.p1_7.abstractengine.render.IShapeRenderer;
import com.p1_7.abstractengine.render.ISpriteBatch;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.core.Rectangle2D;
import com.p1_7.game.core.Transform2D;
import com.p1_7.game.platform.GdxShapeRenderer;

/**
 * level exit door.
 *
 * starts locked (dark brown). call unlock() once the player answers a
 * question correctly; the door turns gold and triggers the ExitDoorHandler
 * when the player walks through it. fires only once (triggered flag).
 *
 * do NOT register with MovementManager (door is static).
 * DO register with CollisionManager.
 */
public class ExitDoor extends Entity implements IRenderItem, ICollidable, ICustomRenderable {

    public static final float DOOR_WIDTH  = 64f;
    public static final float DOOR_HEIGHT = 80f;

    // locked state — dark brown
    private static final float[] COLOUR_LOCKED   = { 0.35f, 0.18f, 0.08f };
    // unlocked state — gold
    private static final float[] COLOUR_UNLOCKED = { 0.90f, 0.75f, 0.10f };
    // door frame colour
    private static final float[] COLOUR_FRAME    = { 0.15f, 0.08f, 0.03f };
    private static final float   FRAME           = 4f;

    private final Transform2D transform;
    private final Rectangle2D bounds = new Rectangle2D();

    private boolean unlocked = false;
    private boolean triggered = false;

    private ExitDoorHandler handler;

    /**
     * @param x left edge in world coordinates
     * @param y bottom edge in world coordinates
     */
    public ExitDoor(float x, float y) {
        super();
        this.transform = new Transform2D(x, y, DOOR_WIDTH, DOOR_HEIGHT);
    }

    /** sets the handler invoked when the player exits through this door. */
    public void setHandler(ExitDoorHandler handler) {
        this.handler = handler;
    }

    /** unlocks the door so the player can pass through. */
    public void unlock() { this.unlocked = true; }

    public boolean isUnlocked() { return unlocked; }

    // ── IRenderItem ──────────────────────────────────────────────────────────

    @Override public String     getAssetPath() { return null; }
    @Override public ITransform getTransform() { return transform; }

    // ── ICollidable ──────────────────────────────────────────────────────────

    @Override
    public IBounds getBounds() {
        bounds.set(
            transform.getPosition(0), transform.getPosition(1),
            DOOR_WIDTH, DOOR_HEIGHT);
        return bounds;
    }

    @Override
    public void onCollision(ICollidable other) {
        if (unlocked && !triggered && other instanceof Player && handler != null) {
            triggered = true;
            handler.onExit();
        }
    }

    // ── ICustomRenderable ────────────────────────────────────────────────────

    /**
     * draws the door frame then the door fill, colour-coded by lock state.
     * locked = dark brown, unlocked = gold.
     */
    @Override
    public void renderCustom(ISpriteBatch batch, IShapeRenderer shapeRenderer) {
        ShapeRenderer sr = ((GdxShapeRenderer) shapeRenderer).unwrap();

        float x = transform.getPosition(0);
        float y = transform.getPosition(1);

        // frame
        float[] frame = COLOUR_FRAME;
        sr.setColor(frame[0], frame[1], frame[2], 1f);
        sr.rect(x - FRAME, y - FRAME, DOOR_WIDTH + FRAME * 2, DOOR_HEIGHT + FRAME * 2);

        // fill
        float[] fill = unlocked ? COLOUR_UNLOCKED : COLOUR_LOCKED;
        sr.setColor(fill[0], fill[1], fill[2], 1f);
        sr.rect(x, y, DOOR_WIDTH, DOOR_HEIGHT);
    }
}
