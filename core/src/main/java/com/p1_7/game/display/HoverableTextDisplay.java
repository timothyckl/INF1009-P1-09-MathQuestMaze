package com.p1_7.game.display;

import com.p1_7.abstractengine.collision.IBounds;
import com.p1_7.abstractengine.collision.ICollidable;
import com.p1_7.abstractengine.render.IShapeRenderer;
import com.p1_7.abstractengine.render.ISpriteBatch;
import com.p1_7.game.entities.MousePointer;

/*
An extension of TextDisplay that includes hover state visual feedback using our Collision System to detect overlap with MousePointer
 */
public class HoverableTextDisplay extends TextDisplay implements ICollidable {

    /** Tracks if a collision with the mouse occurred this frame */
    private boolean isHovered = false;

    public HoverableTextDisplay(String text, float x, float y, float scale) {
        super(text, x, y, scale);
    }

    /**
     * Constructs a display with explicit collision dimensions.
     */
    public HoverableTextDisplay(String text, float x, float y, float width, float height, float scale) {
        super(text, x, y, scale);
        // Apply dimensions to the inherited protected transform
        this.transform.setSize(0, width);
        this.transform.setSize(1, height);
    }

    /**
     * Checks if the entity is currently hovered.
     * @return true if hovered
     */
    public boolean isHovered() {
        return this.isHovered;
    }

    /**
     * Generates a bounding box that is shifted downward.
     * Since LibGDX font.draw renders text downwards from the Y coordinate,
     * we must offset the physics box to match the visual letters.
     */
    @Override
    public IBounds getBounds() {
        float x = transform.getPosition(0);
        float y = transform.getPosition(1);
        float w = transform.getSize(0) == 0 ? 300 : transform.getSize(0);
        float h = transform.getSize(1) == 0 ? 30 : transform.getSize(1);
        
        // Offset Y by height and a small nudge (6px) to center on the text
        return new MousePointer.SimpleBounds(x, y - h, w, h);
    }

    /**
     * Triggered by the Engine's CollisionManager when the MousePointer 
     * overlaps this entity's bounds.
     */
    @Override
    public void onCollision(ICollidable other) {
        if (other instanceof MousePointer) {
            this.isHovered = true; 
        }
    }

    @Override
    public void renderCustom(ISpriteBatch batch, IShapeRenderer shapeRenderer) {
        // Change color based on the current hover state
        if (isHovered) {
            font.setColor(1f, 1f, 0f, 1f); // Yellow highlight
        } else {
            font.setColor(1f, 1f, 1f, 1f); // Default white
        }
        
        // Render text via the base TextDisplay logic
        super.renderCustom(batch, shapeRenderer);

        // Consume-on-Render: Reset the state so that the CollisionManager 
        // must re-verify the overlap in the next update cycle.
        this.isHovered = false;
    }
}