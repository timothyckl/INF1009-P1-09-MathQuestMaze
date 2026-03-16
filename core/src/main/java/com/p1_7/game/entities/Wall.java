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
 * static maze wall.
 *
 * walls block player and enemy movement via collision response in those
 * entities' onCollision() methods. walls themselves are immovable and
 * have no collision response of their own.
 *
 * rendered as a filled rectangle using the procedural (shape) render path —
 * no asset file required. swap getAssetPath() to return a texture path
 * and remove ICustomRenderable if you want a textured wall sprite instead.
 *
 * do NOT register walls with MovementManager (they don't move).
 * DO register them with CollisionManager.
 */
public class Wall extends Entity implements IRenderItem, ICollidable, ICustomRenderable {

    /** default wall color — dark slate, readable on most backgrounds. */
    private static final float R = 0.25f, G = 0.25f, B = 0.30f;

    private final Transform2D transform;
    private final Rectangle2D bounds = new Rectangle2D();

    /**
     * @param x      left edge in world coordinates
     * @param y      bottom edge in world coordinates
     * @param width  wall width in pixels
     * @param height wall height in pixels
     */
    public Wall(float x, float y, float width, float height) {
        super();
        this.transform = new Transform2D(x, y, width, height);
    }

    // ── IRenderItem ──────────────────────────────────────────────────────────

    /** null → engine uses the procedural (ICustomRenderable) render path. */
    @Override public String     getAssetPath() { return null; }
    @Override public ITransform getTransform() { return transform; }

    // ── ICollidable ──────────────────────────────────────────────────────────

    @Override
    public IBounds getBounds() {
        bounds.set(
            transform.getPosition(0), transform.getPosition(1),
            transform.getSize(0),     transform.getSize(1));
        return bounds;
    }

    /** walls are static — collision response is handled by the moving entity. */
    @Override
    public void onCollision(ICollidable other) {}

    // ── ICustomRenderable ────────────────────────────────────────────────────

    /**
     * draws the wall as a filled rectangle.
     *
     * the engine enters ShapeRenderer.Filled mode before calling this and
     * restores it after, so we only need to set the colour and draw.
     */
    @Override
    public void renderCustom(ISpriteBatch batch, IShapeRenderer shapeRenderer) {
        ShapeRenderer sr = ((GdxShapeRenderer) shapeRenderer).unwrap();
        sr.setColor(R, G, B, 1f);
        sr.rect(
            transform.getPosition(0), transform.getPosition(1),
            transform.getSize(0),     transform.getSize(1));
    }
}
