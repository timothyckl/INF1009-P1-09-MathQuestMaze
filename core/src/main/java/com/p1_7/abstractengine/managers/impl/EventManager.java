package com.p1_7.abstractengine.managers.impl;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.p1_7.abstractengine.events.Event;
import com.p1_7.abstractengine.events.EventListener;
import com.p1_7.abstractengine.managers.base.AbstractManager;

/**
 * central event broker that manages event subscriptions and publishing.
 * implements the publish/subscribe pattern for loose coupling between
 * components.
 *
 * uses exact-match dispatch: when an event is published, only listeners
 * subscribed to that exact event class are notified.
 *
 * supports owner-based subscription tracking: subscribers can pass themselves
 * as an owner when subscribing, then call unsubscribeAll(owner) to remove all
 * their subscriptions at once. this avoids repetitive bookkeeping in each
 * subscribing class.
 *
 * note that this event manager is synchronous.
 */
public class EventManager extends AbstractManager {

    /**
     * wraps a listener with its owner for tracking purposes.
     */
    private record OwnedListener(EventListener<?> listener, Object owner) {
    }

    // maps event class types to their registered listeners
    private final ObjectMap<Class<? extends Event>, Array<OwnedListener>> listeners;

    // flag to prevent modification during iteration
    private boolean publishing;

    // pending operations to process after publishing completes
    private final Array<Runnable> pendingOperations;

    public EventManager() {
        listeners = new ObjectMap<>();
        pendingOperations = new Array<>();
        publishing = false;
    }

    @Override
    protected void onInit() {
        // no special initialisation required
    }

    @Override
    protected void onShutdown() {
        listeners.clear();
        pendingOperations.clear();
    }

    /**
     * subscribes a listener to events of the specified type.
     * the listener will only receive events of the exact type specified.
     *
     * @param <T>       the event type
     * @param eventType the event class to listen for
     * @param listener  the listener to register
     */
    public <T extends Event> void subscribe(Class<T> eventType, EventListener<T> listener) {
        subscribe(eventType, listener, null);
    }

    /**
     * subscribes a listener to events of the specified type, associated with an owner.
     * the listener will only receive events of the exact type specified.
     * use unsubscribeAll(owner) to remove all listeners registered by this owner.
     *
     * @param <T>       the event type
     * @param eventType the event class to listen for
     * @param listener  the listener to register
     * @param owner     the owner of this subscription (can be null)
     */
    public <T extends Event> void subscribe(Class<T> eventType, EventListener<T> listener, Object owner) {
        if (eventType == null || listener == null) {
            return;
        }

        OwnedListener owned = new OwnedListener(listener, owner);

        Runnable operation = () -> {
            Array<OwnedListener> eventListeners = listeners.get(eventType);
            if (eventListeners == null) {
                eventListeners = new Array<>();
                listeners.put(eventType, eventListeners);
            }

            // prevent duplicate subscriptions (check by listener reference)
            for (OwnedListener existing : eventListeners) {
                if (existing.listener == listener) {
                    return;
                }
            }
            eventListeners.add(owned);
        };

        if (publishing) {
            pendingOperations.add(operation);
        } else {
            operation.run();
        }
    }

    /**
     * unsubscribes a listener from events of the specified type.
     *
     * @param <T>       the event type
     * @param eventType the event class to unsubscribe from
     * @param listener  the listener to remove
     */
    public <T extends Event> void unsubscribe(Class<T> eventType, EventListener<T> listener) {
        if (eventType == null || listener == null) {
            return;
        }

        Runnable operation = () -> {
            Array<OwnedListener> eventListeners = listeners.get(eventType);
            if (eventListeners != null) {
                for (int i = eventListeners.size - 1; i >= 0; i--) {
                    if (eventListeners.get(i).listener == listener) {
                        eventListeners.removeIndex(i);
                        break;
                    }
                }
            }
        };

        if (publishing) {
            pendingOperations.add(operation);
        } else {
            operation.run();
        }
    }

    /**
     * unsubscribes all listeners registered by the specified owner.
     * this is the recommended way to clean up subscriptions, as it removes
     * all listeners in one call without requiring the subscriber to track them.
     *
     * @param owner the owner whose listeners should be removed
     */
    public void unsubscribeAll(Object owner) {
        if (owner == null) {
            return;
        }

        Runnable operation = () -> {
            for (Array<OwnedListener> eventListeners : listeners.values()) {
                for (int i = eventListeners.size - 1; i >= 0; i--) {
                    if (eventListeners.get(i).owner == owner) {
                        eventListeners.removeIndex(i);
                    }
                }
            }
        };

        if (publishing) {
            pendingOperations.add(operation);
        } else {
            operation.run();
        }
    }

    /**
     * publishes an event to all registered listeners of its exact type.
     *
     * @param event the event to publish
     */
    @SuppressWarnings("unchecked")
    public void publish(Event event) {
        if (event == null) {
            return;
        }

        Array<OwnedListener> eventListeners = listeners.get(event.getClass());
        if (eventListeners == null || eventListeners.size == 0) {
            return;
        }

        publishing = true;
        try {
            for (OwnedListener owned : eventListeners) {
                ((EventListener<Event>) owned.listener).onEvent(event);
            }
        } finally {
            publishing = false;
            processPendingOperations();
        }
    }

    /**
     * processes any subscribe/unsubscribe operations that occurred during
     * publishing.
     */
    private void processPendingOperations() {
        for (Runnable operation : pendingOperations) {
            operation.run();
        }
        pendingOperations.clear();
    }

    /**
     * returns the number of listeners registered for a specific event type.
     *
     * @param eventType the event class to check
     * @return the number of registered listeners
     */
    public int getListenerCount(Class<? extends Event> eventType) {
        Array<OwnedListener> eventListeners = listeners.get(eventType);
        return eventListeners != null ? eventListeners.size : 0;
    }

    /**
     * removes all listeners for a specific event type.
     *
     * @param eventType the event class to clear
     */
    public void clearListeners(Class<? extends Event> eventType) {
        if (eventType == null) {
            return;
        }

        Runnable operation = () -> listeners.remove(eventType);

        if (publishing) {
            pendingOperations.add(operation);
        } else {
            operation.run();
        }
    }

    @Override
    public String toString() {
        int totalListeners = 0;
        for (Array<OwnedListener> arr : listeners.values()) {
            totalListeners += arr.size;
        }
        return "EventManager{eventTypes=" + listeners.size + ", totalListeners=" + totalListeners + "}";
    }
}
