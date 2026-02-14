package com.p1_7.demo;

import com.p1_7.abstractengine.input.ActionId;

/**
 * defines logical input actions for the demo game.
 *
 * these action constants are bound to physical keys in the demo's
 * Main.java and queried by game entities to determine player intent.
 */
public class DemoActions {

    /** move bucket left */
    public static final ActionId LEFT = new ActionId("LEFT");

    /** move bucket right */
    public static final ActionId RIGHT = new ActionId("RIGHT");

    // private constructor prevents instantiation
    private DemoActions() {
    }
}
