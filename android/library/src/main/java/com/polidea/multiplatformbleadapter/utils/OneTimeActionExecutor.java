package com.polidea.multiplatformbleadapter.utils;

public abstract class OneTimeActionExecutor<T> {

    private boolean wasExecuted = false;

    protected abstract void action(T data);

    public synchronized void execute(T data) {
        if (!wasExecuted) {
            action(data);
            wasExecuted = true;
        }
    }
}
