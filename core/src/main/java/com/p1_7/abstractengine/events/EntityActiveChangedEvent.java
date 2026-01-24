package com.p1_7.abstractengine.events;

import com.p1_7.abstractengine.core.Entity;

/**
 * event published when an entity's active state changes.
 *
 * @param entity the entity whose active state changed
 * @param active the new active state
 */
public record EntityActiveChangedEvent(Entity entity, boolean active) implements EntityEvent {
}
