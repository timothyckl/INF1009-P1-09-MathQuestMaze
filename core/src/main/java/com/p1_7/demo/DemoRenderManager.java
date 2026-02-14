package com.p1_7.demo;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import com.p1_7.abstractengine.render.IRenderItem;
import com.p1_7.abstractengine.render.RenderManager;
import com.p1_7.abstractengine.transform.ITransform;

/**
 * demo-specific render manager that handles text rendering for
 * LivesDisplay and TextDisplay entities.
 *
 * implements custom procedural rendering logic to support BitmapFont
 * text drawing which requires switching from ShapeRenderer to SpriteBatch
 * mid-pass.
 */
public class DemoRenderManager extends RenderManager {

    /**
     * handles custom text rendering for demo-specific text display entities.
     * switches from shapeRenderer to batch for BitmapFont drawing.
     *
     * @param item the render item to potentially handle
     * @param batch the sprite batch for text rendering
     * @param shapeRenderer the shape renderer to pause/resume
     * @return true if item was a text display and was rendered, false otherwise
     */
    @Override
    protected boolean renderCustomProcedural(
        IRenderItem item,
        SpriteBatch batch,
        ShapeRenderer shapeRenderer
    ) {
        // handle text rendering for LivesDisplay
        if (item instanceof LivesDisplay) {
            LivesDisplay display = (LivesDisplay) item;
            renderText(display.getFont(), display.getText(),
                      item.getTransform(), batch, shapeRenderer);
            return true;
        }

        // handle text rendering for TextDisplay
        if (item instanceof TextDisplay) {
            TextDisplay display = (TextDisplay) item;
            renderText(display.getFont(), display.getText(),
                      item.getTransform(), batch, shapeRenderer);
            return true;
        }

        // not handled, let RenderManager draw as rectangle
        return false;
    }

    /**
     * renders text using BitmapFont by switching rendering contexts.
     *
     * @param font the bitmap font to use
     * @param text the text string to draw
     * @param transform the transform providing position
     * @param batch the sprite batch for drawing
     * @param shapeRenderer the shape renderer to pause/resume
     */
    private void renderText(
        BitmapFont font,
        String text,
        ITransform transform,
        SpriteBatch batch,
        ShapeRenderer shapeRenderer
    ) {
        float[] position = transform.getPosition();

        // text rendering requires ending shape renderer and starting batch
        shapeRenderer.end();
        batch.begin();
        font.draw(batch, text, position[0], position[1]);
        batch.end();
        shapeRenderer.begin(ShapeType.Filled);
    }
}
