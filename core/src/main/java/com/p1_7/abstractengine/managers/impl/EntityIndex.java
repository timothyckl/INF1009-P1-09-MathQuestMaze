package com.p1_7.abstractengine.managers.impl;

import java.util.Set;
import java.util.UUID;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.p1_7.abstractengine.core.AbstractProperty;
import com.p1_7.abstractengine.core.Entity;
import com.p1_7.abstractengine.core.EntityRepository;
import com.p1_7.abstractengine.core.Tag;
import com.p1_7.abstractengine.events.EntityActiveChangedEvent;
import com.p1_7.abstractengine.events.EntityAddedEvent;
import com.p1_7.abstractengine.events.EntityPropertyChangedEvent;
import com.p1_7.abstractengine.events.EntityRemovedEvent;
import com.p1_7.abstractengine.events.EntityTagChangedEvent;

/**
 * Provides efficient querying of entities by maintaining secondary indices
 * on tags, property types, and active state.
 */
public class EntityIndex {
    private final EntityRepository repo;
    private final ObjectMap<Tag, ObjectSet<UUID>> byTag = new ObjectMap<>();
    private final ObjectMap<Class<? extends AbstractProperty>, ObjectSet<UUID>> byProperty = new ObjectMap<>();
    private final ObjectSet<UUID> active = new ObjectSet<>();

    // event subscription
    private EventManager eventManager;

    /**
     * Creates a new entity index backed by the specified repository.
     *
     * @param repo the entity repository to index
     * @throws IllegalArgumentException if repo is null
     */
    public EntityIndex(EntityRepository repo) {
        if (repo == null) {
            throw new IllegalArgumentException("repo cannot be null");
        }
        this.repo = repo;
    }

    // ========================================================================
    // event subscription
    // ========================================================================

    /**
     * subscribes this index to entity events from the given EventManager.
     * enables automatic index updates when entities are added, removed, or change
     * state.
     *
     * @param eventManager the event manager to subscribe to
     */
    public void subscribeToEvents(EventManager eventManager) {
        if (eventManager == null) {
            return;
        }

        // unsubscribe from previous event manager if any
        unsubscribeFromEvents();

        this.eventManager = eventManager;

        // subscribe to each entity event type with 'this' as owner
        eventManager.subscribe(EntityAddedEvent.class, e -> indexEntity(e.entity()), this);
        eventManager.subscribe(EntityRemovedEvent.class, e -> unindexEntity(e.entity()), this);
        eventManager.subscribe(EntityActiveChangedEvent.class, e -> updateEntityActive(e.entity()), this);
        eventManager.subscribe(EntityTagChangedEvent.class, e -> reindexEntity(e.entity()), this);
        eventManager.subscribe(EntityPropertyChangedEvent.class, e -> reindexEntity(e.entity()), this);
    }

    /**
     * unsubscribes this index from entity events.
     * call this during cleanup or before switching event managers.
     */
    public void unsubscribeFromEvents() {
        if (eventManager == null) {
            return;
        }

        eventManager.unsubscribeAll(this);
        eventManager = null;
    }

    // ========================================================================
    // helper methods
    // ========================================================================

    /**
     * Computes the intersection of multiple UUID sets.
     * Returns entities present in ALL provided sets.
     *
     * @param sets the sets to intersect
     * @return a new ObjectSet containing only UUIDs present in all input sets
     */
    @SafeVarargs
    private final ObjectSet<UUID> intersect(ObjectSet<UUID>... sets) {
        ObjectSet<UUID> result = new ObjectSet<>();

        // handle empty or null input
        if (sets == null || sets.length == 0) {
            return result;
        }

        // find the smallest non-null set to start with (optimisation)
        ObjectSet<UUID> smallest = null;
        for (ObjectSet<UUID> set : sets) {
            if (set != null && (smallest == null || set.size < smallest.size)) {
                smallest = set;
            }
        }

        // if no valid sets, return empty
        if (smallest == null) {
            return result;
        }

        // copy smallest set to result
        for (UUID id : smallest) {
            result.add(id);
        }

        // remove elements not present in all other sets
        for (ObjectSet<UUID> set : sets) {
            if (set == null || set == smallest) {
                continue;
            }
            ObjectSet.ObjectSetIterator<UUID> iterator = result.iterator();
            while (iterator.hasNext()) {
                UUID id = iterator.next();
                if (!set.contains(id)) {
                    iterator.remove();
                }
            }
        }

        return result;
    }

    /**
     * Computes the union of multiple UUID sets.
     * Returns entities present in ANY of the provided sets.
     *
     * @param sets the sets to union
     * @return a new ObjectSet containing all UUIDs from all input sets
     */
    @SafeVarargs
    private final ObjectSet<UUID> union(ObjectSet<UUID>... sets) {
        ObjectSet<UUID> result = new ObjectSet<>();

        // handle empty or null input
        if (sets == null || sets.length == 0) {
            return result;
        }

        // add all elements from all sets
        for (ObjectSet<UUID> set : sets) {
            if (set == null) {
                continue;
            }
            for (UUID id : set) {
                result.add(id);
            }
        }

        return result;
    }

    /**
     * Resolves a set of UUIDs to their corresponding Entity objects.
     *
     * @param ids        the set of entity UUIDs
     * @param activeOnly if true, only returns active entities
     * @return array of resolved entities
     */
    private Array<Entity> resolveEntities(ObjectSet<UUID> ids, boolean activeOnly) {
        Array<Entity> entities = new Array<>();

        if (ids == null) {
            return entities;
        }

        for (UUID id : ids) {
            Entity entity = repo.getEntity(id);
            if (entity != null && (!activeOnly || entity.isActive())) {
                entities.add(entity);
            }
        }

        return entities;
    }

    // ========================================================================
    // index maintenance methods
    // ========================================================================

    /**
     * Rebuilds all indices from the repository.
     * Clears existing indices and re-indexes all entities.
     */
    public void rebuild() {
        byTag.clear();
        byProperty.clear();
        active.clear();

        for (Entity entity : repo.getAllEntities()) {
            if (entity == null) {
                continue;
            }
            indexEntity(entity);
        }
    }

    /**
     * Adds an entity to all relevant indices.
     *
     * @param entity the entity to index
     */
    public void indexEntity(Entity entity) {
        if (entity == null) {
            return;
        }

        UUID id = entity.getID();

        // index by tags
        for (Tag tag : entity.getTags()) {
            ObjectSet<UUID> tagSet = byTag.get(tag);
            if (tagSet == null) {
                tagSet = new ObjectSet<>();
                byTag.put(tag, tagSet);
            }
            tagSet.add(id);
        }

        // index by property types
        Set<Class<? extends AbstractProperty>> propertyTypes = entity.getPropertyTypes();
        for (Class<? extends AbstractProperty> propertyType : propertyTypes) {
            ObjectSet<UUID> propertySet = byProperty.get(propertyType);
            if (propertySet == null) {
                propertySet = new ObjectSet<>();
                byProperty.put(propertyType, propertySet);
            }
            propertySet.add(id);
        }

        // track active state
        if (entity.isActive()) {
            active.add(id);
        }
    }

    /**
     * Removes an entity from all indices.
     * Only removes from indices the entity actually belongs to, and cleans up empty
     * sets.
     *
     * @param entity the entity to unindex
     */
    public void unindexEntity(Entity entity) {
        if (entity == null) {
            return;
        }

        UUID id = entity.getID();

        // only remove from tags this entity has
        for (Tag tag : entity.getTags()) {
            ObjectSet<UUID> tagSet = byTag.get(tag);
            if (tagSet != null) {
                tagSet.remove(id);
                if (tagSet.size == 0) {
                    byTag.remove(tag); // cleanup empty set
                }
            }
        }

        // only remove from properties this entity has
        for (Class<? extends AbstractProperty> propType : entity.getPropertyTypes()) {
            ObjectSet<UUID> propSet = byProperty.get(propType);
            if (propSet != null) {
                propSet.remove(id);
                if (propSet.size == 0) {
                    byProperty.remove(propType); // cleanup empty set
                }
            }
        }

        // remove from active set
        active.remove(id);
    }

    /**
     * Updates the active state tracking for an entity.
     *
     * @param entity the entity whose active state changed
     */
    public void updateEntityActive(Entity entity) {
        if (entity == null) {
            return;
        }

        UUID id = entity.getID();

        if (entity.isActive()) {
            active.add(id);
        } else {
            active.remove(id);
        }
    }

    /**
     * Re-indexes an entity by removing it from all indices and adding it back.
     * Used when an entity's tags or properties change.
     *
     * @param entity the entity to re-index
     */
    private void reindexEntity(Entity entity) {
        unindexEntity(entity);
        indexEntity(entity);
    }

    // ========================================================================
    // query methods
    // ========================================================================

    /**
     * Returns entities with the specified tag.
     *
     * @param tag        the tag to match
     * @param activeOnly if true, only returns active entities
     * @return array of matching entities
     */
    public <T extends Enum<T> & Tag> Array<Entity> getEntitiesBy(T tag, boolean activeOnly) {
        if (tag == null) {
            return new Array<>();
        }

        ObjectSet<UUID> taggedIds = byTag.get(tag);
        if (taggedIds == null) {
            return new Array<>();
        }

        if (activeOnly) {
            ObjectSet<UUID> filteredIds = intersect(taggedIds, active);
            return resolveEntities(filteredIds, false);
        }

        return resolveEntities(taggedIds, false);
    }

    /**
     * Returns entities with the specified property type.
     *
     * @param propertyType the property class to match
     * @param activeOnly   if true, only returns active entities
     * @return array of matching entities
     */
    public Array<Entity> getEntitiesBy(Class<? extends AbstractProperty> propertyType, boolean activeOnly) {
        if (propertyType == null) {
            return new Array<>();
        }

        ObjectSet<UUID> propertyIds = byProperty.get(propertyType);
        if (propertyIds == null) {
            return new Array<>();
        }

        if (activeOnly) {
            ObjectSet<UUID> filteredIds = intersect(propertyIds, active);
            return resolveEntities(filteredIds, false);
        }

        return resolveEntities(propertyIds, false);
    }

    /**
     * Returns entities having all specified tags.
     *
     * @param tags the tags to match (all required)
     * @return array of matching entities
     */
    public Array<Entity> getEntitiesWithAll(Tag... tags) {
        if (tags == null || tags.length == 0) {
            return new Array<>();
        }

        // collect the UUID sets for each tag
        @SuppressWarnings("unchecked")
        ObjectSet<UUID>[] tagSets = new ObjectSet[tags.length];

        for (int i = 0; i < tags.length; i++) {
            if (tags[i] == null) {
                return new Array<>(); // null tag means no match possible
            }
            tagSets[i] = byTag.get(tags[i]);
            if (tagSets[i] == null) {
                return new Array<>(); // missing tag set means no match possible
            }
        }

        ObjectSet<UUID> intersectedIds = intersect(tagSets);
        return resolveEntities(intersectedIds, false);
    }

    /**
     * Returns entities having all specified property types.
     *
     * @param propertyTypes the property classes to match (all required)
     * @return array of matching entities
     */
    @SafeVarargs
    public final Array<Entity> getEntitiesWithAll(Class<? extends AbstractProperty>... propertyTypes) {
        if (propertyTypes == null || propertyTypes.length == 0) {
            return new Array<>();
        }

        @SuppressWarnings("unchecked")
        ObjectSet<UUID>[] propertySets = new ObjectSet[propertyTypes.length];

        for (int i = 0; i < propertyTypes.length; i++) {
            if (propertyTypes[i] == null) {
                return new Array<>();
            }
            propertySets[i] = byProperty.get(propertyTypes[i]);
            if (propertySets[i] == null) {
                return new Array<>();
            }
        }

        ObjectSet<UUID> intersectedIds = intersect(propertySets);
        return resolveEntities(intersectedIds, false);
    }

    /**
     * Returns entities having any of the specified tags.
     *
     * @param tags the tags to match (at least one required)
     * @return array of matching entities
     */
    public Array<Entity> getEntitiesWithAny(Tag... tags) {
        if (tags == null || tags.length == 0) {
            return new Array<>();
        }

        // collect non-null tag sets
        Array<ObjectSet<UUID>> tagSetsList = new Array<>();

        for (Tag tag : tags) {
            if (tag == null) {
                continue;
            }
            ObjectSet<UUID> tagSet = byTag.get(tag);
            if (tagSet != null) {
                tagSetsList.add(tagSet);
            }
        }

        if (tagSetsList.size == 0) {
            return new Array<>();
        }

        @SuppressWarnings("unchecked")
        ObjectSet<UUID>[] tagSetsArray = tagSetsList.toArray(ObjectSet.class);
        ObjectSet<UUID> unionedIds = union(tagSetsArray);
        return resolveEntities(unionedIds, false);
    }

    /**
     * Returns entities having any of the specified property types.
     *
     * @param propertyTypes the property classes to match (at least one required)
     * @return array of matching entities
     */
    @SafeVarargs
    public final Array<Entity> getEntitiesWithAny(Class<? extends AbstractProperty>... propertyTypes) {
        if (propertyTypes == null || propertyTypes.length == 0) {
            return new Array<>();
        }

        Array<ObjectSet<UUID>> propertySetsList = new Array<>();

        for (Class<? extends AbstractProperty> propertyType : propertyTypes) {
            if (propertyType == null) {
                continue;
            }
            ObjectSet<UUID> propertySet = byProperty.get(propertyType);
            if (propertySet != null) {
                propertySetsList.add(propertySet);
            }
        }

        if (propertySetsList.size == 0) {
            return new Array<>();
        }

        @SuppressWarnings("unchecked")
        ObjectSet<UUID>[] propertySetsArray = propertySetsList.toArray(ObjectSet.class);
        ObjectSet<UUID> unionedIds = union(propertySetsArray);
        return resolveEntities(unionedIds, false);
    }

    /**
     * Counts entities with the specified tag.
     *
     * @param tag the tag to count
     * @return the number of entities with the tag
     */
    public <T extends Enum<T> & Tag> int countEntitiesBy(T tag) {
        if (tag == null) {
            return 0;
        }

        ObjectSet<UUID> tagSet = byTag.get(tag);
        if (tagSet == null) {
            return 0;
        }

        return tagSet.size;
    }

    /**
     * Counts entities with the specified property type.
     *
     * @param propertyType the property class to count
     * @return the number of entities with the property type
     */
    public int countEntitiesBy(Class<? extends AbstractProperty> propertyType) {
        if (propertyType == null) {
            return 0;
        }

        ObjectSet<UUID> propertySet = byProperty.get(propertyType);
        if (propertySet == null) {
            return 0;
        }

        return propertySet.size;
    }

    /**
     * Returns all active entities.
     *
     * @return array of all active entities
     */
    public Array<Entity> getActiveEntities() {
        return resolveEntities(active, false);
    }

    /**
     * Counts all active entities.
     *
     * @return the number of active entities
     */
    public int countActiveEntities() {
        return active.size;
    }
}
