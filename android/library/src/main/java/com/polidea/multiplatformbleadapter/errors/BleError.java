package com.polidea.multiplatformbleadapter.errors;


public class BleError {

    private BleErrorCode errorCode;
    private Integer androidCode;
    private String reason;

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
}
