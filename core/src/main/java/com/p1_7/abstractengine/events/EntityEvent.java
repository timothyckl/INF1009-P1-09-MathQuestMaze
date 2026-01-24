package com.p1_7.abstractengine.events;

import com.p1_7.abstractengine.core.Entity;

/**
 * sealed interface for all entity-related events.
 *
 * using a sealed interface enables exhaustive pattern matching in switch
 * expressions, ensuring compile-time safety when handling all entity event types.
 */
public sealed interface EntityEvent extends Event
        permits EntityAddedEvent, EntityRemovedEvent, EntityActiveChangedEvent,
                EntityTagChangedEvent, EntityPropertyChangedEvent {

    /**
     * returns the entity associated with this event.
     *
     * @return the entity this event relates to
     */
    Entity entity();
}
