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

    /**
     * called by the engine during the wiring pass to give this manager a resolver
     * for looking up its declared dependencies. default is a no-op; Manager
     * overrides this via the setDependencies / onWire template-method pair.
     *
     * @param resolver the resolver used to look up dependency instances
     */
    default void setDependencies(ManagerResolver resolver) {
        // no-op — Manager subclasses handle this via onWire()
    }
}
