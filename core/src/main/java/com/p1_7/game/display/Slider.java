package com.p1_7.game.display;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.p1_7.abstractengine.collision.IBounds;
import com.p1_7.abstractengine.collision.ICollidable;
import com.p1_7.abstractengine.entity.Entity;
import com.p1_7.abstractengine.render.ICustomRenderable;
import com.p1_7.abstractengine.render.IRenderItem;
import com.p1_7.abstractengine.render.IShapeRenderer;
import com.p1_7.abstractengine.render.ISpriteBatch;
import com.p1_7.abstractengine.transform.ITransform;
import com.p1_7.game.core.Transform2D;
import com.p1_7.game.entities.MousePointer;
import com.p1_7.game.platform.GdxShapeRenderer;

/**
 * Procedural Slider entity that implements ICollidable.
 * Uses the engine's CollisionManager to detect hover states via onCollision.
 */
public class Slider extends Entity implements IRenderItem, ICustomRenderable, ICollidable {
    
    private final Transform2D transform;
    private float value; // ranges from 0.0 to 1.0
    private boolean isHovered = false;

    public Slider(float x, float y, float width, float height, float initialValue) {
        this.transform = new Transform2D(x, y, width, height);
        this.value = Math.max(0f, Math.min(1f, initialValue));
    }

    public float getValue() { return value; }
    public void setValue(float value) { this.value = Math.max(0f, Math.min(1f, value)); }

    @Override
    public String getAssetPath() { return null; }

    @Override
    public ITransform getTransform() { return transform; }

    /**
     * Provides the bounding box for collision detection.
     * Unlike text, sliders draw upward from the origin, so no Y-offset is needed.
     */
    @Override
    public IBounds getBounds() {
        return new MousePointer.SimpleBounds(
            transform.getPosition(0),
            transform.getPosition(1),
            transform.getSize(0),
            transform.getSize(1)
        );
    }

    /**
     * Triggered by the Engine's CollisionManager.
     */
    @Override
    public void onCollision(ICollidable other) {
        if (other instanceof MousePointer) {
            this.isHovered = true;
        }
    }

    @Override
    public void renderCustom(ISpriteBatch batch, IShapeRenderer shapeRenderer) {
        ShapeRenderer sr = ((GdxShapeRenderer) shapeRenderer).unwrap();

        float posX = transform.getPosition(0);
        float posY = transform.getPosition(1);
        float width = transform.getSize(0);
        float height = transform.getSize(1);

        // 1. Draw the background track
        // Brighten the track if the mouse is currently hovering
        if (isHovered) {
            sr.setColor(0.4f, 0.4f, 0.4f, 1f); 
        } else {
            sr.setColor(0.3f, 0.3f, 0.3f, 1f);
        }
        sr.rect(posX, posY, width, height);

        // 2. Draw the filled foreground track
        if (isHovered) {
            sr.setColor(1.0f, 1.0f, 1.0f, 1f); // Pure white highlight
        } else {
            sr.setColor(0.8f, 0.8f, 0.8f, 1f); // Off-white
        }
        sr.rect(posX, posY, width * value, height);

        // Consume-on-Render Pattern: 
        // Reset state so it must be re-validated by the CollisionManager next frame.
        this.isHovered = false;
    }
}