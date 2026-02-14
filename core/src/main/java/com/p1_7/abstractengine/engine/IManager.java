package com.p1_7.abstractengine.engine;

/**
 * core lifecycle contract for every manager in the abstract engine.
 *
 * implementations are responsible for allocating and releasing any
 * resources they hold during init() and shutdown()
 * respectively.
 */
public interface IManager {

    /**
     * initialises the manager and allocates any required resources.
     */
    void init();

    /**
     * shuts down the manager and releases all held resources.
     */
    void shutdown();
}
