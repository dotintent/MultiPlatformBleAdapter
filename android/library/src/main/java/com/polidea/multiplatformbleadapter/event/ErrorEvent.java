package com.polidea.multiplatformbleadapter.event;


public class ErrorEvent extends Event {

    private Throwable error;

    public ErrorEvent(Throwable error) {
        this.error = error;
    }

    public Throwable getError() {
        return error;
    }
}
