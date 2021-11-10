package com.polidea.multiplatformbleadapter.errors;


public class BleError extends Throwable {

    public final BleErrorCode errorCode;
    public final Integer androidCode;
    public final String reason;
    public String deviceID;
    public String serviceUUID;
    public String characteristicUUID;
    public String descriptorUUID;
    public String internalMessage;

    public BleError(BleErrorCode errorCode, String reason, Integer androidCode) {
        this.errorCode = errorCode;
        this.reason = reason;
        this.androidCode = androidCode;
    }

    @Override
    public String getMessage() {
        return "Error code: " + errorCode +
                ", android code: " + androidCode +
                ", reason" + reason +
                ", deviceId" + deviceID +
                ", serviceUuid" + serviceUUID +
                ", characteristicUuid" + characteristicUUID +
                ", descriptorUuid" + descriptorUUID +
                ", internalMessage" + internalMessage;
    }
}
