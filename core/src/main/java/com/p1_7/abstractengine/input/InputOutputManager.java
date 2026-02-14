package com.p1_7.abstractengine.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;

import com.p1_7.abstractengine.engine.UpdatableManager;

/**
 * manages per-frame input polling and exposes the resulting action
 * states via IInputQuery.
 *
 * each frame the manager iterates every value in ActionId,
 * checks whether any bound key or button is physically pressed, and
 * derives the frame-local transition state (InputState)
 * relative to the previous frame. the entire pipeline is dormant
 * until ActionId is populated with real values and bindings
 * are added to the InputMapping.
 */
public class InputOutputManager extends UpdatableManager implements IInputQuery {

    /** the key/button ↔ action mapping used for lookups */
    private final InputMapping inputMapping = new InputMapping();

    /** whether input polling is enabled */
    private boolean inputEnabled = true;

    /** the derived input state for each action this frame */
    private final ObjectMap<ActionId, InputState> actionStates = new ObjectMap<>();

    /** whether each action was physically down last frame */
    private final ObjectMap<ActionId, Boolean> previousDown = new ObjectMap<>();

    // ---------------------------------------------------------------
    // UpdatableManager hook
    // ---------------------------------------------------------------

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

        // collect all unique action ids from the input mapping
        ObjectSet<ActionId> boundActions = getBoundActions();

        for (ActionId action : boundActions) {
            // determine whether any bound physical input is down
            boolean currentlyDown = isPhysicallyDown(action);

            // retrieve the state from last frame (defaults to false)
            boolean wasDown = previousDown.get(action, false);

            // derive the transition and store it
            if (currentlyDown && !wasDown) {
                actionStates.put(action, InputState.PRESSED);
            } else if (currentlyDown && wasDown) {
                actionStates.put(action, InputState.HELD);
            } else if (!currentlyDown && wasDown) {
                actionStates.put(action, InputState.RELEASED);
            } else {
                // not active this frame — remove any stale entry
                actionStates.remove(action);
            }

            // record the raw state for next frame
            previousDown.put(action, currentlyDown);
        }
    }

    // ---------------------------------------------------------------
    // IInputQuery
    // ---------------------------------------------------------------

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

    // ---------------------------------------------------------------
    // accessors
    // ---------------------------------------------------------------

    /**
     * returns the input mapping used by this manager.
     *
     * @return the current InputMapping
     */
    public InputMapping getInputMapping() {
        return inputMapping;
    }

    /**
     * enables or disables input polling. when disabled,
     * actionStates is cleared each frame so that all
     * queries return inactive.
     *
     * @param enabled true to enable polling
     */
    public void setInputEnabled(boolean enabled) {
        this.inputEnabled = enabled;
    }

    // ---------------------------------------------------------------
    // private helpers
    // ---------------------------------------------------------------

    /**
     * collects all unique action ids that have been bound to at least
     * one key or button in the input mapping.
     *
     * @return an ObjectSet of all bound actions
     */
    private ObjectSet<ActionId> getBoundActions() {
        return inputMapping.getAllActions();
    }

    /**
     * checks whether any physical key or button bound to the given
     * action is currently held down, using the reverse-lookup helpers
     * on the InputMapping.
     *
     * @param action the logical action to check
     * @return true if at least one bound input is pressed
     */
    private boolean isPhysicallyDown(ActionId action) {
        // check all bound keyboard keys
        Array<Integer> keys = inputMapping.getKeysForAction(action);
        for (int i = 0; i < keys.size; i++) {
            if (Gdx.input.isKeyPressed(keys.get(i))) {
                return true;
            }
        }

        // check all bound controller buttons
        Array<Integer> buttons = inputMapping.getButtonsForAction(action);
        for (int i = 0; i < buttons.size; i++) {
            if (Gdx.input.isButtonPressed(buttons.get(i))) {
                return true;
            }
        }

        return false;
    }
}
