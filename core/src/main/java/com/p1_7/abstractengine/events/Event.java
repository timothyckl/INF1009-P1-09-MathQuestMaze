package com.p1_7.abstractengine.events;

/**
 * base marker interface for all events in the system.
 *
 * this interface is intentionally unsealed to allow users to define
 * their own custom events by implementing this interface directly.
 *
 * engine-provided event families (like EntityEvent) are sealed
 * to enable exhaustive pattern matching within that family.
 */
public interface Event {
    // marker interface for all events
}
