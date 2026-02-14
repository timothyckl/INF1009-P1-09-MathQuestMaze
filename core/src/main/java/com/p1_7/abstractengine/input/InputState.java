package com.p1_7.abstractengine.input;

/**
 * represents the discrete state of a logical input action within a
 * single frame.
 *
 * - PRESSED - the action transitioned from inactive to active this frame.
 * - HELD - the action was already active last frame and remains active.
 * - RELEASED - the action transitioned from active to inactive this frame.
 */
public enum InputState {

    /** action just became active this frame */
    PRESSED,

    /** action has been held since a previous frame */
    HELD,

    /** action just became inactive this frame */
    RELEASED
}
