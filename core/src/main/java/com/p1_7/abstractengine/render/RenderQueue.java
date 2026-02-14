package com.p1_7.abstractengine.render;

import com.badlogic.gdx.utils.Array;

/**
 * simple array-backed implementation of IRenderQueue.
 * one instance is held for the lifetime of the RenderManager.
 */
class RenderQueue implements IRenderQueue {

    /** the backing store for queued items */
    private final Array<IRenderItem> items = new Array<>();

    /**
     * adds an item to the queue for drawing this frame.
     *
     * @param item the render item to enqueue
     */
    @Override
    public void queue(IRenderItem item) {
        items.add(item);
    }

    /**
     * removes all items from the queue.
     */
    @Override
    public void clear() {
        items.clear();
    }

    /**
     * returns the backing array so that the render manager can
     * iterate it.
     *
     * @return the array of queued render items
     */
    @Override
    public Array<IRenderItem> items() {
        return items;
    }
}
