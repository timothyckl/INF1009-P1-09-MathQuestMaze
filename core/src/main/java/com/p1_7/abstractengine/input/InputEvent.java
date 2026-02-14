package com.p1_7.abstractengine.input;

/**
 * abstract base class for all input events produced by the engine.
 *
 * concrete event types (digital, analogue, pointer, etc.) are
 * context-specific and belong in the demo phase. this class defines
 * the fields and accessors that every event shares: the logical
 * ActionId that triggered it and the system timestamp at
 * which it occurred.
 */
public abstract class InputEvent {

    /** the logical action associated with this event */
    private final ActionId actionId;

    /** the system timestamp (in milliseconds) when the event occurred */
    private final long timestamp;

    /**
     * constructs an input event with the given action and timestamp.
     *
     * @param actionId  the logical action that triggered this event
     * @param timestamp the system time in milliseconds
     */
    protected InputEvent(ActionId actionId, long timestamp) {
        this.actionId = actionId;
        this.timestamp = timestamp;
    }

    /**
     * returns the logical action associated with this event.
     *
     * @return the action identifier; never null
     */
    public ActionId getActionId() {
        return actionId;
    }

    /**
     * returns the system timestamp at which this event occurred.
     *
     * @return the timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }
}
