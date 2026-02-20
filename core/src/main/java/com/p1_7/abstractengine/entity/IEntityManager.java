package com.p1_7.abstractengine.entity;

/**
 * unified contract for the entity store, combining read and write access.
 *
 * code that needs both querying and mutation receives this interface.
 * read-only access is available through IEntityRepository; write-only
 * access is available through IEntityMutator. implementations may
 * selectively fulfil either or both contracts based on their needs.
 */
public interface IEntityManager extends IEntityRepository, IEntityMutator {
}
