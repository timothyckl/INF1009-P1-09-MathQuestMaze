package com.p1_7.abstractengine.render;

/**
 * a single-frame accumulator for items that should be drawn this tick.
 *
 * scenes submit IRenderItem instances each frame via
 * queue(IRenderItem). the RenderManager consumes
 * the queue during the draw pass and then calls clear().
 * implementations are provided internally by the render manager.
 */
public interface IRenderQueue {

    /**
     * adds an item to the queue for drawing this frame.
     *
     * @param item the render item to enqueue
     */
    void queue(IRenderItem item);

    /**
     * removes all items from the queue. called by the render manager
     * after the draw pass completes.
     */
    void clear();

    /**
     * returns an iterable over every item currently in the queue.
     *
     * @return an iterable of queued render items
     */
    Iterable<IRenderItem> items();
}
