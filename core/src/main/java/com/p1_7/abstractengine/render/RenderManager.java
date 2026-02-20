package com.p1_7.abstractengine.render;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import com.p1_7.abstractengine.engine.Manager;
import com.p1_7.abstractengine.transform.ITransform;

/**
 * owns the drawing resources and drives the per-frame render pass for all
 * queued items.
 */
public class RenderManager extends Manager {

    /** sprite batch used for textured items */
    protected SpriteBatch batch;

    /** shape renderer used for procedural items */
    protected ShapeRenderer shapeRenderer;

    /** asset manager for texture loading and caching */
    protected AssetManager assetManager;

    /** single-frame queue of items to draw */
    private final RenderQueue queue = new RenderQueue();

    /**
     * creates the SpriteBatch, ShapeRenderer, and
     * AssetManager resources.
     */
    @Override
    protected void onInit() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        assetManager = new AssetManager();
    }

    /**
     * disposes the SpriteBatch, ShapeRenderer, and
     * AssetManager resources.
     */
    @Override
    protected void onShutdown() {
        // dispose asset manager (disposes all loaded assets)
        if (assetManager != null) { assetManager.dispose(); }
        if (batch != null) { batch.dispose(); }
        if (shapeRenderer != null) { shapeRenderer.dispose(); }
    }

    /**
     * returns the render queue that scenes use to submit items for
     * drawing.
     *
     * @return the IRenderQueue instance
     */
    public IRenderQueue getRenderQueue() {
        return queue;
    }

    /**
     * executes the full render pass for the current frame, then clears the queue.
     */
    public void render() {
        assetManager.finishLoading();

        batch.begin();
        for (IRenderItem item : queue.items()) {
            if (item.getAssetPath() != null) {
                drawTextured(item);
            }
        }
        batch.end();

        shapeRenderer.begin(ShapeType.Filled);
        for (IRenderItem item : queue.items()) {
            if (item.getAssetPath() == null) {
                drawProcedural(item);
            }
        }
        shapeRenderer.end();

        queue.clear();
    }

    /**
     * draws a textured item using its asset path.
     *
     * @param item the render item with a non-null asset path
     */
    private void drawTextured(IRenderItem item) {
        String assetPath = item.getAssetPath();

        if (assetPath == null) {
            return;
        }

        // blocking load on first access
        if (!assetManager.isLoaded(assetPath, Texture.class)) {
            assetManager.load(assetPath, Texture.class);
            assetManager.finishLoadingAsset(assetPath);
        }

        Texture texture = assetManager.get(assetPath, Texture.class);
        ITransform transform = item.getTransform();
        float[] position = transform.getPosition();
        float[] size = transform.getSize();

        batch.draw(texture, position[0], position[1], size[0], size[1]);
    }

    /**
     * hook for custom procedural rendering; delegates to ICustomRenderable if applicable.
     *
     * @param item the render item to potentially handle
     * @param batch the sprite batch (not currently active)
     * @param shapeRenderer the shape renderer (currently active)
     * @return true if the item was handled, false to fall back to rectangle drawing
     */
    protected boolean renderCustomProcedural(
        IRenderItem item,
        SpriteBatch batch,
        ShapeRenderer shapeRenderer
    ) {
        if (item instanceof ICustomRenderable) {
            ((ICustomRenderable) item).renderCustom(batch, shapeRenderer);
            return true;
        }

        return false;
    }

    /**
     * draws the item procedurally, falling back to a filled rectangle.
     *
     * @param item the render item to draw procedurally
     */
    private void drawProcedural(IRenderItem item) {
        boolean handled = renderCustomProcedural(item, batch, shapeRenderer);

        if (!handled) {
            ITransform transform = item.getTransform();
            float[] position = transform.getPosition();
            float[] size = transform.getSize();

            float x = position[0];
            float y = position[1];
            float w = size[0];
            float h = size[1];

            shapeRenderer.rect(x, y, w, h);
        }
    }
}
