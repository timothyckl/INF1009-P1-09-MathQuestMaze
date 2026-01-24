package com.p1_7.abstractengine.events;

/**
 * functional interface for event listeners.
 *
 * listeners subscribe to specific event types and receive events
 * of that exact type when published.
 *
 * @param <T> the event type this listener handles
 */
@FunctionalInterface
public interface EventListener<T extends Event> {

    /**
     * handles the received event.
     *
     * @param event the event to process
     */
    void onEvent(T event);
}
