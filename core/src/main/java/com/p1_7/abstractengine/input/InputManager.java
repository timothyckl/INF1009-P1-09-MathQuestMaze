package com.p1_7.abstractengine.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;

import com.p1_7.abstractengine.engine.UpdatableManager;

/**
 * polls physical input devices each frame and exposes derived action states
 * via IInputQuery.
 */
public class InputManager extends UpdatableManager implements IInputQuery {

    /** the key/button ↔ action mapping used for lookups */
    private final InputMapping inputMapping = new InputMapping();

    /** whether input polling is enabled */
    private boolean inputEnabled = true;

    /** the derived input state for each action this frame */
    private final ObjectMap<ActionId, InputState> actionStates = new ObjectMap<>();

    /** whether each action was physically down last frame */
    private final ObjectMap<ActionId, Boolean> previousDown = new ObjectMap<>();

    /**
     * polls the physical input devices and computes the logical
     * action-state transitions for this frame.
     *
     * @param deltaTime seconds elapsed since the previous frame
     */
    @Override
    protected void onUpdate(float deltaTime) {
        if (!inputEnabled) {
            actionStates.clear();
            return;
        }

        ObjectSet<ActionId> boundActions = getBoundActions();

        for (ActionId action : boundActions) {
            boolean currentlyDown = isPhysicallyDown(action);
            boolean wasDown = previousDown.get(action, false);

            if (currentlyDown && !wasDown) {
                actionStates.put(action, InputState.PRESSED);
            } else if (currentlyDown && wasDown) {
                actionStates.put(action, InputState.HELD);
            } else if (!currentlyDown && wasDown) {
                actionStates.put(action, InputState.RELEASED);
            } else {
                actionStates.remove(action);
            }

            previousDown.put(action, currentlyDown);
        }
    }

    /**
     * returns whether the specified action is currently active
     * (either InputState.PRESSED or InputState.HELD).
     *
     * @param actionId the logical action to query
     * @return true if the action is active this frame
     */
    @Override
    public boolean isActionActive(ActionId actionId) {
        InputState state = actionStates.get(actionId);
        return state == InputState.PRESSED || state == InputState.HELD;
    }

    /**
     * returns the precise input state for the specified action.
     *
     * @param actionId the logical action to query
     * @return the InputState, or null if inactive
     */
    @Override
    public InputState getActionState(ActionId actionId) {
        return actionStates.get(actionId);
    }

    /**
     * returns the input mapping used by this manager.
     *
     * @return the current InputMapping
     */
    public InputMapping getInputMapping() {
        return inputMapping;
    }

    /**
     * enables or disables input polling.
     *
     * @param enabled true to enable polling
     */
    public void setInputEnabled(boolean enabled) {
        this.inputEnabled = enabled;
    }

    /**
     * returns all action ids that have at least one key or button binding.
     *
     * @return an ObjectSet of all bound actions
     */
    private ObjectSet<ActionId> getBoundActions() {
        return inputMapping.getAllActions();
    }

    /**
     * returns true if any physical key or button bound to the action is currently held.
     *
     * @param action the logical action to check
     * @return true if at least one bound input is pressed
     */
    private boolean isPhysicallyDown(ActionId action) {
        Array<Integer> keys = inputMapping.getKeysForAction(action);
        for (int i = 0; i < keys.size; i++) {
            if (Gdx.input.isKeyPressed(keys.get(i))) {
                return true;
            }
        }

        Array<Integer> buttons = inputMapping.getButtonsForAction(action);
        for (int i = 0; i < buttons.size; i++) {
            if (Gdx.input.isButtonPressed(buttons.get(i))) {
                return true;
            }
        }

        return false;
    }
}
