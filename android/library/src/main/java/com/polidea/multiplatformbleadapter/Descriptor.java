package com.polidea.multiplatformbleadapter;

import android.bluetooth.BluetoothGattDescriptor;
import android.support.annotation.NonNull;

import com.polidea.multiplatformbleadapter.utils.ByteUtils;
import com.polidea.multiplatformbleadapter.utils.IdGenerator;
import com.polidea.multiplatformbleadapter.utils.IdGeneratorKey;
import com.polidea.rxandroidble.internal.RxBleLog;

import java.util.UUID;

public class Descriptor {
    private int characteristicId;
    private int serviceId;
    private UUID characteristicUuid;
    private UUID serviceUuid;
    private String deviceId;
    private BluetoothGattDescriptor descriptor;
    private int id;
    private UUID uuid;
    private byte[] value = null;

    public Descriptor(@NonNull Characteristic characteristic, @NonNull BluetoothGattDescriptor gattDescriptor) {
        this.characteristicId = characteristic.getId();
        this.characteristicUuid = characteristic.getUuid();
        this.serviceId = characteristic.getServiceID();
        this.serviceUuid = characteristic.getServiceUUID();
        this.descriptor = gattDescriptor;
        this.deviceId = characteristic.getDeviceId();
        this.id = IdGenerator.getIdForKey(new IdGeneratorKey(deviceId, descriptor.getUuid(), characteristicId));
        this.uuid = gattDescriptor.getUuid();
    }

    public int getId() {
        return id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public int getCharacteristicId() {
        return characteristicId;
    }

    public int getServiceId() {
        return serviceId;
    }

    public UUID getCharacteristicUuid() {
        return characteristicUuid;
    }

    public UUID getServiceUuid() {
        return serviceUuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    public void setValueFromCache() {
        value = descriptor.getValue();
    }

    public BluetoothGattDescriptor getNativeDescriptor() {
        return descriptor;
    }

    public void logValue(String message, byte[] value) {
        if (value == null) {
            value = descriptor.getValue();
        }
        String hexValue = value != null ? ByteUtils.bytesToHex(value) : "(null)";
        RxBleLog.v(message +
                " Descriptor(uuid: " + descriptor.getUuid().toString() +
                ", id: " + id +
                ", value: " + hexValue + ")");
    }
}