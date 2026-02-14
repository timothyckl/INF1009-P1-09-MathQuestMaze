package com.p1_7.abstractengine.entity;

import java.util.UUID;

/**
 * write-side contract for the entity store.
 *
 * code that needs to add, update or remove entities receives this
 * interface. read-only access is provided by IEntityRepository.
 * ownership of an entity moves to the manager once
 * createEntity(EntityFactory) returns.
 */
public interface IEntityMutator {

    /**
     * creates a new entity via the supplied factory, adds it to the
     * store, and returns it.
     *
     * @param factory the factory that constructs the concrete entity
     * @return the newly created and registered entity
     */
    Entity createEntity(EntityFactory factory);

    /**
     * updates the active flag on the entity identified by id.
     *
     * @param id     the UUID of the target entity
     * @param active the desired active state
     */
    void updateEntity(UUID id, boolean active);

    /**
     * removes the entity identified by id from the store.
     *
     * @param id the UUID of the entity to remove
     */
    void removeEntity(UUID id);
}
