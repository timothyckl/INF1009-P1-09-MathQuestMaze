package com.p1_7.abstractengine.engine;

/**
 * abstract base class providing the standard init/shutdown lifecycle template
 * for all managers.
 */
public abstract class Manager implements IManager {

    /** tracks whether this manager has been initialised */
    private boolean initialised;

    /**
     * initialises the manager.
     */
    @Override
    public final void init() {
        initialised = true;
        onInit();
    }

    /**
     * shuts down the manager.
     */
    @Override
    public final void shutdown() {
        onShutdown();
        initialised = false;
    }

    /**
     * override to perform setup work; default is a no-op.
     */
    protected void onInit() {
        // no-op — subclasses may override
    }

    /**
     * override to release resources; default is a no-op.
     */
    protected void onShutdown() {
        // no-op — subclasses may override
    }

    /**
     * returns whether this manager has been initialised.
     *
     * @return true if the manager is currently initialised
     */
    public boolean isInitialised() {
        return initialised;
    }
}
