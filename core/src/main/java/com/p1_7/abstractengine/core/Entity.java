package com.p1_7.abstractengine.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.badlogic.gdx.utils.Array;
import com.p1_7.abstractengine.properties.Dimension;
import com.p1_7.abstractengine.properties.Position;

public class Entity extends AbstractObject implements Taggable {
    private final UUID id;
    private final Set<Tag> tags;

    public Entity() {
        tags = new HashSet<>();
        id = UUID.randomUUID();
        active = false;
        properties = new Array<AbstractProperty>();
        initialiseProperties();
    }

    @Override
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the active state of this entity.
     *
     * @param active true to activate, false to deactivate
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public void addProperty(AbstractProperty property) {
        if (property == null) {
            return;
        }
        // remove existing property of same type to prevent duplicates
        AbstractProperty existing = getProperty(property.getClass());
        if (existing != null) {
            properties.removeValue(existing, true);
        }
        properties.add(property);
    }

    @Override
    public void removeProperty(AbstractProperty property) {
        properties.removeValue(property, true);
    }

    @Override
    public <T extends AbstractProperty> T getProperty(Class<T> type) {
        if (type == null) {
            return null;
        }
        for (AbstractProperty property : properties) {
            if (type.isInstance(property)) {
                return type.cast(property);
            }
        }
        return null;
    }

    /**
     * Checks whether this entity has a property of the specified type.
     *
     * @param type the property class to check for
     * @return true if the entity has a property of the given type, false otherwise
     */
    public boolean hasProperty(Class<? extends AbstractProperty> type) {
        if (type == null) {
            return false;
        }
        return getProperty(type) != null;
    }

    /**
     * Returns the set of property class types this entity contains.
     *
     * @return an unmodifiable set of property class types
     */
    public Set<Class<? extends AbstractProperty>> getPropertyTypes() {
        Set<Class<? extends AbstractProperty>> types = new HashSet<>();
        for (AbstractProperty property : properties) {
            if (property != null) {
                types.add(property.getClass());
            }
        }
        return Collections.unmodifiableSet(types);
    }

    public UUID getID() {
        return id;
    }

    /**
     * Initialises properties for this entity. Subclasses should override to add
     * properties.
     */
    protected void initialiseProperties() {
        properties.add(new Dimension());
        properties.add(new Position());
    }

    // taggable interface implementation

    @Override
    public <T extends Enum<T> & Tag> void addTag(T tag) {
        if (tag != null) {
            tags.add(tag);
        }
    }

    @Override
    public <T extends Enum<T> & Tag> void removeTag(T tag) {
        if (tag != null) {
            tags.remove(tag);
        }
    }

    @Override
    public <T extends Enum<T> & Tag> boolean hasTag(T tag) {
        if (tag == null) {
            return false;
        }
        return tags.contains(tag);
    }

    @Override
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    @Override
    public void clearTags() {
        tags.clear();
    }

    /**
     * Checks whether this entity has all of the specified tags.
     *
     * @param tagsToCheck the tags to check for
     * @return true if the entity has all specified tags, false otherwise
     */
    public boolean hasAllTags(Tag... tagsToCheck) {
        if (tagsToCheck == null || tagsToCheck.length == 0) {
            return true;
        }
        for (Tag tag : tagsToCheck) {
            if (!tags.contains(tag)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether this entity has any of the specified tags.
     *
     * @param tagsToCheck the tags to check for
     * @return true if the entity has at least one of the specified tags, false
     *         otherwise
     */
    public boolean hasAnyTag(Tag... tagsToCheck) {
        if (tagsToCheck == null || tagsToCheck.length == 0) {
            return false;
        }
        for (Tag tag : tagsToCheck) {
            if (tags.contains(tag)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Entity{id=" + id + ", active=" + active + ", properties=" + properties.size + ", tags=" + tags.size()
                + "}";
    }
}
