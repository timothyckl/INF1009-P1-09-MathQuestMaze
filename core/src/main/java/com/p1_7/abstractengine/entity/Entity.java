package com.p1_7.abstractengine.entity;

import java.util.UUID;

/**
 * abstract base class for every entity managed by the engine.
 *
 * each entity is assigned a globally unique identifier at
 * construction time and is active by default. concrete subclasses
 * (created during the demo phase) extend this class to attach
 * transform, movability, collidability and other capabilities.
 */
public abstract class Entity {

    /** unique identifier assigned at construction */
    private final UUID id;

    /** whether this entity participates in engine updates */
    private boolean active;

    /**
     * constructs a new entity with a randomly generated UUID and
     * active set to true.
     */
    protected Entity() {
        this.id = UUID.randomUUID();
        this.active = true;
    }

    /**
     * returns the unique identifier of this entity.
     *
     * @return the entity's UUID; never null
     */
    public UUID getId() {
        return id;
    }

    /**
     * returns whether this entity is currently active.
     *
     * @return true if the entity participates in updates
     */
    public boolean isActive() {
        return active;
    }

    /**
     * sets the active state of this entity.
     *
     * @param active true to activate, false to deactivate
     */
    public void setActive(boolean active) {
        this.active = active;
    }
}
