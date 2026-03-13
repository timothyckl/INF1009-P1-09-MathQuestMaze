package com.p1_7.game.platform;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.p1_7.abstractengine.input.IInputSource;

public class GdxInputSource extends InputAdapter implements IInputSource {

    /**
    pseudo button for mouse scroll
    our InputManager only accounts for Keys and Buttons pressed, but Libgdx reads mouse scroll as "Scrolled"
    So SCROLL_UP and SCROLL_DOWN function as 'Pressed' buttons for the scroll wheel.
    **/
    public static final int SCROLL_UP = -101; // negative so it doesn't clash with real button codes
    public static final int SCROLL_DOWN = -102;

    private boolean scrolledUp = false;
    private boolean scrolledDown = false;

    public GdxInputSource() {
        Gdx.input.setInputProcessor(this); // registering this class for scroll wheel
    }

    @Override
    public boolean isKeyPressed(int keyCode) {
        return Gdx.input.isKeyPressed(keyCode);
    }

    @Override
    public boolean isButtonPressed(int buttonCode) {
        //scroll wheel uses "Consume-on-Read"
        if (buttonCode == SCROLL_UP) {
            boolean state = scrolledUp;
            scrolledUp = false; // Consume the event so it only triggers for one frame
            return state;
        }
        if (buttonCode == SCROLL_DOWN) {
            boolean state = scrolledDown;
            scrolledDown = false; // Consume the event
            return state;
        }

        //prevents LibGDX from crashing if given negative pseudo-codes
        if (buttonCode < 0) return false;

        return Gdx.input.isButtonPressed(buttonCode);
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        //amountY > 0 means scroll down (towards user), amountY < 0 means scroll up
        if (amountY < 0) {
            scrolledUp = true;
        } else if (amountY > 0) {
            scrolledDown = true;
        }
        return true;
    }
}