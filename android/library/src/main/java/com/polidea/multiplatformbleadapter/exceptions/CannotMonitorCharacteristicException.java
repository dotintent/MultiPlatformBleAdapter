package com.polidea.multiplatformbleadapter.exceptions;

import com.polidea.multiplatformbleadapter.Characteristic;

public class CannotMonitorCharacteristicException extends RuntimeException {
    private final Characteristic characteristic;

    public CannotMonitorCharacteristicException(Characteristic characteristic) {
        this.characteristic = characteristic;
    }

    public Characteristic getCharacteristic() {
        return characteristic;
    }
}
