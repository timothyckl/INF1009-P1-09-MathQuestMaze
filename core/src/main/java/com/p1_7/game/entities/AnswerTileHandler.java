package com.p1_7.game.entities;

/**
 * callback invoked by AnswerTile when the player steps on it.
 *
 * implement this in the GameScene to update score, health, and
 * question state based on whether the player chose correctly.
 */
@FunctionalInterface
public interface AnswerTileHandler {

    /**
     * called when the player collides with an answer tile.
     *
     * @param correct true if this tile holds the right answer
     * @param value   the numeric value displayed on the tile
     */
    void onAnswer(boolean correct, int value);
}
