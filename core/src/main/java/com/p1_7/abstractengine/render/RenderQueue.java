package com.p1_7.abstractengine.render;

import java.util.ArrayList;
import java.util.List;

/**
 * list-backed implementation of IRenderQueue.
 */
class RenderQueue implements IRenderQueue {

    /** the backing store for queued items */
    private final List<IRenderItem> items = new ArrayList<>();

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
     * @return the list of queued render items
     */
    @Override
    public List<IRenderItem> items() {
        return items;
    }
}
