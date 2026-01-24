package com.p1_7.abstractengine.events;

import com.p1_7.abstractengine.core.Entity;

/**
 * event published when an entity is added to the entity manager.
 *
 * @param entity the entity that was added
 */
public record EntityAddedEvent(Entity entity) implements EntityEvent {
}
