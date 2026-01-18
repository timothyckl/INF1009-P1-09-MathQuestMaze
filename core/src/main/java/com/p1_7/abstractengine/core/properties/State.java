package com.p1_7.abstractengine.core.properties;

import com.p1_7.abstractengine.core.AbstractProperty;

public class State<T> extends AbstractProperty {
    private T currentState;
    private T previousState;

    public State(T initialState) {
        this.currentState = initialState;
        this.previousState = null;
    }

    public T getState() {
        return currentState;
    }

    public void setState(T state) {
        this.previousState = this.currentState;
        this.currentState = state;
    }

    public T getPreviousState() {
        return currentState;
    }

    public boolean hasChanged() {
        if (previousState == null) {
            return false;
        }
        return !currentState.equals(previousState);
    }

}
