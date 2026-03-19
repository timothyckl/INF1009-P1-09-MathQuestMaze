package com.p1_7.game.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.Input;
import com.p1_7.abstractengine.input.ActionId;
import com.p1_7.abstractengine.input.InputBindingSpec;

/**
 * Canonical remappable game actions and their default keyboard bindings.
 */
public final class GameActions {

    public static final ActionId MOVE_UP    = new ActionId("MOVE_UP");
    public static final ActionId MOVE_DOWN  = new ActionId("MOVE_DOWN");
    public static final ActionId MOVE_LEFT  = new ActionId("MOVE_LEFT");
    public static final ActionId MOVE_RIGHT = new ActionId("MOVE_RIGHT");

    private static final List<BindingSpec> MOVEMENT_BINDINGS = createMovementBindings();

    private GameActions() { }

    /**
     * Returns the ordered movement bindings shown in the settings remap table.
     *
     * @return immutable ordered movement binding definitions
     */
    public static List<BindingSpec> getMovementBindings() {
        return MOVEMENT_BINDINGS;
    }

    /**
     * Returns the default movement bindings in an engine-level format that can
     * be applied by InputManager during construction.
     *
     * @return immutable engine-level default bindings
     */
    public static List<InputBindingSpec> getDefaultBindings() {
        List<InputBindingSpec> bindings = new ArrayList<>();
        for (int i = 0; i < MOVEMENT_BINDINGS.size(); i++) {
            BindingSpec binding = MOVEMENT_BINDINGS.get(i);
            bindings.add(InputBindingSpec.keys(binding.getActionId(),
                binding.getPrimaryKeyCode(),
                binding.getAlternateKeyCode()));
        }
        return Collections.unmodifiableList(bindings);
    }

    private static List<BindingSpec> createMovementBindings() {
        List<BindingSpec> bindings = new ArrayList<>();
        bindings.add(new BindingSpec("Move Up", MOVE_UP, Input.Keys.W, Input.Keys.UP));
        bindings.add(new BindingSpec("Move Down", MOVE_DOWN, Input.Keys.S, Input.Keys.DOWN));
        bindings.add(new BindingSpec("Move Left", MOVE_LEFT, Input.Keys.A, Input.Keys.LEFT));
        bindings.add(new BindingSpec("Move Right", MOVE_RIGHT, Input.Keys.D, Input.Keys.RIGHT));
        return Collections.unmodifiableList(bindings);
    }

    /**
     * Definition of one remappable action row.
     */
    public static final class BindingSpec {
        private final String   label;
        private final ActionId actionId;
        private final int      primaryKeyCode;
        private final int      alternateKeyCode;

        BindingSpec(String label, ActionId actionId, int primaryKeyCode, int alternateKeyCode) {
            this.label            = label;
            this.actionId         = actionId;
            this.primaryKeyCode   = primaryKeyCode;
            this.alternateKeyCode = alternateKeyCode;
        }

        public String getLabel() {
            return label;
        }

        public ActionId getActionId() {
            return actionId;
        }

        public int getPrimaryKeyCode() {
            return primaryKeyCode;
        }

        public int getAlternateKeyCode() {
            return alternateKeyCode;
        }
    }
}
