package com.p1_7.game.input;

import com.p1_7.abstractengine.input.ActionId;

/**
 * logical input actions for Math Quest Maze.
 *
 * bind these to physical keys in Main.java via InputMapping,
 * then query them in Player.updateMovement() each frame.
 *
 * example bindings (in Main.java):
 *   inputMapping.bindKey(Keys.W, GameActions.UP);
 *   inputMapping.bindKey(Keys.S, GameActions.DOWN);
 *   inputMapping.bindKey(Keys.A, GameActions.LEFT);
 *   inputMapping.bindKey(Keys.D, GameActions.RIGHT);
 */
public class GameActions {

    public static final ActionId UP    = new ActionId("UP");
    public static final ActionId DOWN  = new ActionId("DOWN");
    public static final ActionId LEFT  = new ActionId("LEFT");
    public static final ActionId RIGHT = new ActionId("RIGHT");

    private GameActions() {}
}
