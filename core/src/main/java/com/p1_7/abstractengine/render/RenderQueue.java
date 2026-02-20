package com.p1_7.abstractengine.render;

import com.badlogic.gdx.utils.Array;

/**
 * array-backed implementation of IRenderQueue.
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
     * returns the queued render items.
     *
     * @return the array of queued render items
     */
    @Override
    public Array<IRenderItem> items() {
        return items;
    }
}
