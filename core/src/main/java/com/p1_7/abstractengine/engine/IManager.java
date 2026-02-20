package com.p1_7.abstractengine.engine;

/**
 * lifecycle contract for every manager; implementations allocate resources
 * in init() and release them in shutdown().
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
