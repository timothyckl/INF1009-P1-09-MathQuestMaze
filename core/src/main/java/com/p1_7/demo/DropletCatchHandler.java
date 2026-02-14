package com.p1_7.demo;

/**
 * functional interface for handling droplet catch events.
 *
 * invoked by the bucket when it collides with a falling droplet,
 * allowing the game scene to respond with score updates, sound effects,
 * and entity management.
 */
@FunctionalInterface
public interface DropletCatchHandler {

    /**
     * handles a droplet being caught by the bucket.
     *
     * @param droplet the droplet that was caught
     */
    void handleCatch(Droplet droplet);
}
