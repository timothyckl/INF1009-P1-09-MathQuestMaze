package com.p1_7.game.input;

import com.p1_7.abstractengine.input.ActionId;

public class MappableActions {
    
    // move left 
    public static ActionId LEFT = new ActionId("LEFT");

    // move right
    public static ActionId RIGHT = new ActionId("RIGHT");

    //move up
    public static ActionId UP = new ActionId("UP");

    //move down
    public static ActionId DOWN = new ActionId("DOWN");

    // pseudo scroll up
    public static ActionId SCROLL_UP = new ActionId("SCROLL_UP");

    // pseudo scroll down
    public static ActionId SCROLL_DOWN = new ActionId("SCROLL_DOWN");

    private MappableActions() {
    }
}
