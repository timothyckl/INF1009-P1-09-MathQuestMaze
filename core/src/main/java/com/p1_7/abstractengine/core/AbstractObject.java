package com.p1_7.abstractengine.core;

import com.badlogic.gdx.utils.Array;

public abstract class AbstractObject {
    protected boolean active;
    protected Array<AbstractProperty> properties;

    public abstract boolean isActive();

    public abstract void addProperty(AbstractProperty property);

    public abstract void removeProperty(AbstractProperty property);

    public abstract <T extends AbstractProperty> T getProperty(Class<T> type);
}
