package com.p1_7.abstractengine.events;

import com.p1_7.abstractengine.core.Entity;
import com.p1_7.abstractengine.core.Tag;

/**
 * event published when an entity's tags are modified.
 *
 * @param entity the entity whose tags changed
 * @param tag    the tag that was added or removed
 * @param added  true if the tag was added, false if removed
 */
public record EntityTagChangedEvent(Entity entity, Tag tag, boolean added) implements EntityEvent {
}
