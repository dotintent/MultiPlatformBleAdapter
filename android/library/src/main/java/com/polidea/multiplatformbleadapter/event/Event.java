package com.polidea.multiplatformbleadapter.event;

public abstract class Event {

    public String getName() {
        return this.getClass().getSimpleName();
    }
}