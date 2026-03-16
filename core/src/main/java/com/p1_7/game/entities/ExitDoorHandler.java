package com.p1_7.game.entities;

/**
 * callback invoked by ExitDoor when the player walks through an unlocked door.
 *
 * implement this in the GameScene to trigger the level-complete transition.
 */
@FunctionalInterface
public interface ExitDoorHandler {

    /** called when the player enters an unlocked exit door. */
    void onExit();
}
