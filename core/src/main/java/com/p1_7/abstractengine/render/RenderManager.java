package com.p1_7.abstractengine.render;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Array;

import com.p1_7.abstractengine.engine.Manager;
import com.p1_7.abstractengine.transform.ITransform;

/**
 * owns the drawing resources and drives the per-frame render pass.
 *
 * this manager extends Manager directly - it has no
 * per-frame update() logic. all drawing happens inside the
 * explicit render() call that the com.p1_7.abstractengine.engine.Engine
 * issues each frame.
 *
 * the render pass is split into two phases:
 * 1. textured phase - items whose
 *    IRenderable.getAssetPath() is non-null are drawn via a
 *    SpriteBatch.
 * 2. procedural phase - items that return
 *    null from getAssetPath() are drawn as filled
 *    rectangles via a ShapeRenderer.
 * after both phases complete the queue is cleared.
 */
public abstract class RenderManager extends Manager {

    /** sprite batch used for textured items */
    protected SpriteBatch batch;

    /** shape renderer used for procedural items */
    protected ShapeRenderer shapeRenderer;

    /** asset manager for texture loading and caching */
    protected AssetManager assetManager;

    /** single-frame queue of items to draw */
    private final RenderQueue queue = new RenderQueue();

    // ---------------------------------------------------------------
    // Manager lifecycle hooks
    // ---------------------------------------------------------------

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

    // ---------------------------------------------------------------
    // public API
    // ---------------------------------------------------------------

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
     * executes the full render pass for the current frame.
     *
     * 1. textured phase: iterates the queue and draws every item
     *    that has a non-null asset path via the SpriteBatch.
     * 2. procedural phase: iterates the queue and draws every item
     *    that has a null asset path as a filled rectangle via the
     *    ShapeRenderer.
     * 3. clears the queue so it is empty for the next frame.
     */
    public void render() {
        // ensure all queued assets are loaded
        assetManager.finishLoading();

        // --- textured pass ---
        batch.begin();
        for (IRenderItem item : queue.items()) {
            if (item.getAssetPath() != null) {
                drawTextured(item);
            }
        }
        batch.end();

        // --- procedural pass ---
        shapeRenderer.begin(ShapeType.Filled);
        for (IRenderItem item : queue.items()) {
            if (item.getAssetPath() == null) {
                drawProcedural(item);
            }
        }
        shapeRenderer.end();

        // --- flush ---
        queue.clear();
    }

    // ---------------------------------------------------------------
    // private drawing helpers
    // ---------------------------------------------------------------

    /**
     * draws a textured or text item. loads textures via AssetManager
     * on first access and draws them with the SpriteBatch. handles
     * special text rendering for LivesDisplay.
     *
     * @param item the render item with a non-null asset path (or text item)
     */
    private void drawTextured(IRenderItem item) {
        String assetPath = item.getAssetPath();

        // skip null paths (handled by procedural pass)
        if (assetPath == null) {
            return;
        }

        // load texture via AssetManager if not already loaded
        if (!assetManager.isLoaded(assetPath, Texture.class)) {
            assetManager.load(assetPath, Texture.class);
            assetManager.finishLoadingAsset(assetPath);  // blocking load
        }

        Texture texture = assetManager.get(assetPath, Texture.class);
        ITransform transform = item.getTransform();
        float[] position = transform.getPosition();
        float[] size = transform.getSize();

        batch.draw(texture, position[0], position[1], size[0], size[1]);
    }

    /**
     * hook for custom procedural rendering logic that requires special handling.
     * called during the procedural pass for items that return null from getAssetPath()
     * and cannot be rendered as simple rectangles.
     *
     * implementations can check item types and perform custom drawing.
     * if this method returns true, the item is considered handled and the default
     * rectangle drawing is skipped. if false, the item will be drawn as a filled rectangle.
     *
     * important: if custom rendering requires switching between shapeRenderer and batch,
     * the implementation must end/begin the renderers correctly.
     *
     * @param item the render item to potentially handle
     * @param batch the sprite batch (not currently active during procedural pass)
     * @param shapeRenderer the shape renderer (currently active with ShapeType.Filled)
     * @return true if the item was handled, false to fall back to rectangle drawing
     */
    protected abstract boolean renderCustomProcedural(
        IRenderItem item,
        SpriteBatch batch,
        ShapeRenderer shapeRenderer
    );

    /**
     * draws a filled rectangle or delegates to custom rendering at the item's transform position.
     * delegates to subclass hook for special rendering (e.g., text).
     * falls back to rectangle drawing if not handled by subclass.
     *
     * @param item the render item to draw procedurally
     */
    private void drawProcedural(IRenderItem item) {
        // delegate to subclass hook for custom rendering
        boolean handled = renderCustomProcedural(item, batch, shapeRenderer);

        // if not handled by custom logic, fall back to rectangle drawing
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
