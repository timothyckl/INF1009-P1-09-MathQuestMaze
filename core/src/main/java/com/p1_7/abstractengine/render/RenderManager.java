package com.p1_7.abstractengine.render;

import com.p1_7.abstractengine.engine.Manager;
import com.p1_7.abstractengine.transform.ITransform;

/**
 * owns the drawing resources and drives the per-frame render pass for all
 * queued items. subclasses provide the platform-specific sprite batch,
 * shape renderer, and asset store via factory methods.
 */
public abstract class RenderManager extends Manager {

    /** sprite batch used for textured items */
    protected ISpriteBatch batch;

    /** shape renderer used for procedural items */
    protected IShapeRenderer shapeRenderer;

    /** asset store for texture loading and caching */
    protected IAssetStore assetStore;

    /** single-frame queue of items to draw */
    private final RenderQueue queue = new RenderQueue();

    /**
     * creates a platform-specific sprite batch.
     *
     * @return a new ISpriteBatch instance
     */
    protected abstract ISpriteBatch createSpriteBatch();

    /**
     * creates a platform-specific shape renderer.
     *
     * @return a new IShapeRenderer instance
     */
    protected abstract IShapeRenderer createShapeRenderer();

    /**
     * creates a platform-specific asset store.
     *
     * @return a new IAssetStore instance
     */
    protected abstract IAssetStore createAssetStore();

    /**
     * creates the drawing resources via the platform factory methods.
     */
    @Override
    protected void onInit() {
        batch = createSpriteBatch();
        shapeRenderer = createShapeRenderer();
        assetStore = createAssetStore();
    }

    /**
     * disposes the drawing resources.
     */
    @Override
    protected void onShutdown() {
        if (assetStore != null) { assetStore.dispose(); }
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
        assetStore.finishLoading();

        // textured pass
        batch.begin();
        for (IRenderItem item : queue.items()) {
            if (item.getAssetPath() != null) {
                drawTextured(item);
            }
        }
        batch.end();

        // procedural pass — only items implementing ICustomRenderable
        shapeRenderer.begin();
        for (IRenderItem item : queue.items()) {
            if (item.getAssetPath() == null && item instanceof ICustomRenderable) {
                ((ICustomRenderable) item).renderCustom(batch, shapeRenderer);
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
        Object textureHandle = assetStore.loadTexture(assetPath);

        ITransform transform = item.getTransform();
        float[] position = transform.getPosition();
        float[] size = transform.getSize();

        batch.draw(textureHandle, position[0], position[1], size[0], size[1]);
    }
}
