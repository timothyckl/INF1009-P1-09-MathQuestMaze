package com.p1_7.abstractengine.engine;

/**
 * abstract base class that provides the standard lifecycle template for
 * all managers in the engine.
 *
 * subclasses override onInit() and onShutdown() to
 * perform their own setup and teardown. the public init() and
 * shutdown() methods are final and must not be
 * overridden - they manage the isInitialised() flag and then
 * delegate to the hooks.
 */
public abstract class Manager implements IManager {

    /** tracks whether this manager has been initialised */
    private boolean initialised;

    /**
     * initialises the manager. sets the initialised flag and delegates
     * to onInit().
     */
    @Override
    public final void init() {
        initialised = true;
        onInit();
    }

    /**
     * shuts down the manager. delegates to onShutdown() and
     * clears the initialised flag.
     */
    @Override
    public final void shutdown() {
        onShutdown();
        initialised = false;
    }

    /**
     * hook called during initialisation. override in subclasses to
     * perform setup work. default implementation is a no-op.
     */
    protected void onInit() {
        // no-op — subclasses may override
    }

    /**
     * hook called during shutdown. override in subclasses to release
     * resources. default implementation is a no-op.
     */
    protected void onShutdown() {
        // no-op — subclasses may override
    }

    /**
     * returns whether this manager has been initialised.
     *
     * @return true if init() has been called and
     *         shutdown() has not yet been called
     */
    public boolean isInitialised() {
        return initialised;
    }
}
