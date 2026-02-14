package com.p1_7.abstractengine.entity;

/**
 * functional interface that decouples the EntityManager from
 * any concrete Entity subclass.
 *
 * the demo phase supplies a concrete implementation - typically a
 * lambda or method reference - that constructs the desired entity
 * type. the manager calls create() and takes ownership of
 * the returned instance.
 */
@FunctionalInterface
public interface EntityFactory {

    /**
     * constructs and returns a new entity instance.
     *
     * @return a freshly created Entity; must not be null
     */
    Entity create();
}
