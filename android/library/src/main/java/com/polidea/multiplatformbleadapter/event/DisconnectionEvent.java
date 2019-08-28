package com.polidea.multiplatformbleadapter.event;

import androidx.annotation.Nullable;

public class DisconnectionEvent extends Event {

    @Nullable
    private Throwable error;

    public DisconnectionEvent(@Nullable Throwable error) {
        this.error = error;
    }

    @Nullable
    public Throwable getError() {
        return error;
    }
}
