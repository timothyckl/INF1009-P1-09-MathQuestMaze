package com.p1_7.abstractengine.core;

import com.badlogic.gdx.utils.Array;

public abstract class AbstractObject {
    protected boolean active;
    protected Array<AbstractProperty> properties;

    public abstract void update(float deltaTime);

    public abstract boolean isActive();

    public abstract void addProperty(AbstractProperty property);

    public abstract void removeProperty(AbstractProperty property);

    // public abstract AbstractElement getProperty(Object type); // note: me thinks this will need a revision.
    public abstract <T extends AbstractProperty> T getProperty(Class<T> type);
}
