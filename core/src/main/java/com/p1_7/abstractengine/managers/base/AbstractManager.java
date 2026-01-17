package com.p1_7.abstractengine.managers.base;

import com.p1_7.abstractengine.managers.api.IManager;

public abstract class AbstractManager implements IManager{
    @Override
    public abstract void init();

    @Override
    public abstract void update(float deltaTime);

    @Override
    public abstract void shutdown();
}
