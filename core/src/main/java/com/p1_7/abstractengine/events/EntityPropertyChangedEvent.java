package com.p1_7.abstractengine.events;

import com.p1_7.abstractengine.core.AbstractProperty;
import com.p1_7.abstractengine.core.Entity;

/**
 * event published when an entity's properties are modified.
 *
 * @param entity   the entity whose properties changed
 * @param property the property that was added or removed
 * @param added    true if the property was added, false if removed
 */
public record EntityPropertyChangedEvent(Entity entity, AbstractProperty property, boolean added) implements EntityEvent {
}
