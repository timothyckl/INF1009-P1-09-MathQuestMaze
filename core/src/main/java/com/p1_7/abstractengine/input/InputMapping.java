package com.p1_7.abstractengine.input;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;

/**
 * maintains the mapping between physical input codes (keyboard keys and
 * controller buttons) and logical ActionId values.
 */
public class InputMapping {

    /** maps keyboard key codes to logical actions */
    private final ObjectMap<Integer, ActionId> keyBindings = new ObjectMap<>();

    /** maps controller button codes to logical actions */
    private final ObjectMap<Integer, ActionId> buttonBindings = new ObjectMap<>();

    public InputMapping() {
        resetToDefaults();
    }

    /**
     * clears all key and button bindings.
     */
    public void resetToDefaults() {
        keyBindings.clear();
        buttonBindings.clear();
    }

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

    /**
     * binds a keyboard key to a logical action.
     *
     * @param keyCode  the libGDX key code
     * @param actionId the action to associate with the key
     * @throws IllegalArgumentException if actionId is null
     */
    public void bindKey(int keyCode, ActionId actionId) {
        if (actionId == null) {
            throw new IllegalArgumentException("actionId cannot be null");
        }
        keyBindings.put(keyCode, actionId);
    }

    /**
     * binds a controller button to a logical action.
     *
     * @param buttonCode the libGDX button code
     * @param actionId   the action to associate with the button
     * @throws IllegalArgumentException if actionId is null
     */
    public void bindButton(int buttonCode, ActionId actionId) {
        if (actionId == null) {
            throw new IllegalArgumentException("actionId cannot be null");
        }
        buttonBindings.put(buttonCode, actionId);
    }

    /**
     * returns every key code mapped to the supplied action.
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
     * returns every button code mapped to the supplied action.
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
     * returns all unique action ids that have at least one binding.
     *
     * @return an ObjectSet of all bound actions
     */
    public ObjectSet<ActionId> getAllActions() {
        ObjectSet<ActionId> actions = new ObjectSet<>();

        for (ObjectMap.Entry<Integer, ActionId> entry : keyBindings) {
            if (entry.value != null) {
                actions.add(entry.value);
            }
        }

        for (ObjectMap.Entry<Integer, ActionId> entry : buttonBindings) {
            if (entry.value != null) {
                actions.add(entry.value);
            }
        }

        return actions;
    }
}
