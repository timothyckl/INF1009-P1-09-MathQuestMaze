package com.p1_7.abstractengine.entity;

import java.util.UUID;

import com.badlogic.gdx.utils.Array;

/**
 * manages the lifecycle of all entities in the engine. acts as both
 * the read-only repository (IEntityRepository) and the
 * write-side mutator (IEntityMutator).
 *
 * this manager extends com.p1_7.abstractengine.engine.Manager
 * directly - it has no per-frame update logic of its own.
 */
public class EntityManager extends com.p1_7.abstractengine.engine.Manager
        implements IEntityRepository, IEntityMutator {

    /** the backing store of all live entities */
    private final Array<Entity> entities = new Array<>();

    // ---------------------------------------------------------------
    // IEntityMutator
    // ---------------------------------------------------------------

    /**
     * creates an entity via the supplied factory, adds it to the
     * internal store, and returns it.
     *
     * @param factory the factory that constructs the concrete entity
     * @return the newly created and registered entity
     */
    @Override
    public Entity createEntity(EntityFactory factory) {
        Entity entity = factory.create();
        entities.add(entity);
        return entity;
    }

    /**
     * sets the active flag on the entity identified by id.
     * performs a linear scan of the entity array.
     *
     * @param id     the UUID of the target entity
     * @param active the desired active state
     */
    @Override
    public void updateEntity(UUID id, boolean active) {
        for (int i = 0; i < entities.size; i++) {
            if (entities.get(i).getId().equals(id)) {
                entities.get(i).setActive(active);
                return;
            }
        }
    }

    /**
     * removes the entity identified by id. performs a linear
     * scan and removes by index.
     *
     * @param id the UUID of the entity to remove
     */
    @Override
    public void removeEntity(UUID id) {
        for (int i = 0; i < entities.size; i++) {
            if (entities.get(i).getId().equals(id)) {
                entities.removeIndex(i);
                return;
            }
        }
    }

    // ---------------------------------------------------------------
    // IEntityRepository
    // ---------------------------------------------------------------

    /**
     * retrieves a single entity by its unique identifier. performs a
     * linear scan.
     *
     * @param id the UUID to look up
     * @return the matching entity, or null if not found
     */
    @Override
    public Entity getEntity(UUID id) {
        for (int i = 0; i < entities.size; i++) {
            if (entities.get(i).getId().equals(id)) {
                return entities.get(i);
            }
        }
        return null;
    }

    /**
     * builds and returns a new Array containing the UUIDs of
     * every entity in the store.
     *
     * @return an Array of all entity identifiers
     */
    @Override
    public Array<UUID> getAllEntityIds() {
        Array<UUID> ids = new Array<>(entities.size);
        for (int i = 0; i < entities.size; i++) {
            ids.add(entities.get(i).getId());
        }
        return ids;
    }

    // ---------------------------------------------------------------
    // diagnostics
    // ---------------------------------------------------------------

    /**
     * returns a debug string listing the UUIDs of all entities
     * currently in the store.
     *
     * @return a human-readable summary of the entity store
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("EntityManager{");
        for (int i = 0; i < entities.size; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(entities.get(i).getId());
        }
        sb.append('}');
        return sb.toString();
    }
}
