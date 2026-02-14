package com.p1_7.abstractengine.input;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;

/**
 * maintains the mapping between physical input codes (keyboard keys,
 * controller buttons) and logical ActionId values.
 *
 * no bindings are populated by default because ActionId
 * currently contains only the placeholder ActionId.NONE.
 * the demo phase will call bindKey(int, ActionId) and
 * bindButton(int, ActionId) to wire up real controls.
 */
public class InputMapping {

    /** maps keyboard key codes to logical actions */
    private final ObjectMap<Integer, ActionId> keyBindings = new ObjectMap<>();

    /** maps controller button codes to logical actions */
    private final ObjectMap<Integer, ActionId> buttonBindings = new ObjectMap<>();

    /**
     * constructs an InputMapping and initialises it to the
     * default (empty) state.
     */
    public InputMapping() {
        resetToDefaults();
    }

    /**
     * clears all key and button bindings. called automatically by the
     * constructor; the demo phase will re-populate bindings after
     * ActionId gains meaningful values.
     */
    public void resetToDefaults() {
        keyBindings.clear();
        buttonBindings.clear();
    }

    // ---------------------------------------------------------------
    // forward lookups
    // ---------------------------------------------------------------

    /**
     * returns the logical action bound to the given keyboard key code.
     *
     * @param keyCode the libGDX key code
     * @return the bound ActionId, or null if unbound
     */
    public ActionId getActionForKey(int keyCode) {
        return keyBindings.get(keyCode);
    }

    /**
     * returns the logical action bound to the given controller button
     * code.
     *
     * @param buttonCode the libGDX button code
     * @return the bound ActionId, or null if unbound
     */
    public ActionId getActionForButton(int buttonCode) {
        return buttonBindings.get(buttonCode);
    }

    // ---------------------------------------------------------------
    // binding
    // ---------------------------------------------------------------

    /**
     * binds a keyboard key to a logical action.
     *
     * @param keyCode  the libGDX key code
     * @param actionId the action to associate with the key
     */
    public void bindKey(int keyCode, ActionId actionId) {
        keyBindings.put(keyCode, actionId);
    }

    /**
     * binds a controller button to a logical action.
     *
     * @param buttonCode the libGDX button code
     * @param actionId   the action to associate with the button
     */
    public void bindButton(int buttonCode, ActionId actionId) {
        buttonBindings.put(buttonCode, actionId);
    }

    // ---------------------------------------------------------------
    // reverse lookups
    // ---------------------------------------------------------------

    /**
     * scans the key-binding map and returns every key code that is
     * mapped to the supplied action.
     *
     * @param actionId the action to search for
     * @return an Array of matching key codes (may be empty)
     */
    public Array<Integer> getKeysForAction(ActionId actionId) {
        Array<Integer> keys = new Array<>();
        for (ObjectMap.Entry<Integer, ActionId> entry : keyBindings) {
            if (entry.value != null && entry.value.equals(actionId)) {
                keys.add(entry.key);
            }
        }
        return keys;
    }

    /**
     * scans the button-binding map and returns every button code that
     * is mapped to the supplied action.
     *
     * @param actionId the action to search for
     * @return an Array of matching button codes (may be empty)
     */
    public Array<Integer> getButtonsForAction(ActionId actionId) {
        Array<Integer> buttons = new Array<>();
        for (ObjectMap.Entry<Integer, ActionId> entry : buttonBindings) {
            if (entry.value != null && entry.value.equals(actionId)) {
                buttons.add(entry.key);
            }
        }
        return buttons;
    }

    /**
     * returns all unique action ids that have been bound to at least
     * one key or button.
     *
     * @return an ObjectSet of all bound actions
     */
    public ObjectSet<ActionId> getAllActions() {
        ObjectSet<ActionId> actions = new ObjectSet<>();

        // collect from key bindings
        for (ObjectMap.Entry<Integer, ActionId> entry : keyBindings) {
            if (entry.value != null) {
                actions.add(entry.value);
            }
        }

        // collect from button bindings
        for (ObjectMap.Entry<Integer, ActionId> entry : buttonBindings) {
            if (entry.value != null) {
                actions.add(entry.value);
            }
        }

        return actions;
    }
}
