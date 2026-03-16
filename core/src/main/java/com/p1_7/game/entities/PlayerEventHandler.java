package com.p1_7.game.entities;

/**
 * callback invoked by Player when a collision with an enemy occurs.
 *
 * implement this in the GameScene to deduct health and apply any
 * invincibility-frame logic centrally rather than inside the entity.
 */
@FunctionalInterface
public interface PlayerEventHandler {

    /** called when the player collides with an enemy. */
    void onEnemyHit();
}
