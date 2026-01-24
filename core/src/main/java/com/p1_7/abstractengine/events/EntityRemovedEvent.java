package com.p1_7.abstractengine.events;

import com.p1_7.abstractengine.core.Entity;

/**
 * event published when an entity is removed from the entity manager.
 *
 * @param entity the entity that was removed
 */
public record EntityRemovedEvent(Entity entity) implements EntityEvent {
}
