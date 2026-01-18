package com.p1_7.abstractengine.core;

public abstract class AbstractProperty{
    protected boolean enabled;

    public boolean isEnabled(){
        return enabled;
    }

    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }
}
