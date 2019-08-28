package com.polidea.multiplatformbleadapter.event;

public class AdapterStateChangeEvent extends Event {

    private int adapterState;

    public AdapterStateChangeEvent(int adapterState) {
        this.adapterState = adapterState;
    }

    public int getAdapterState() {
        return adapterState;
    }
}
