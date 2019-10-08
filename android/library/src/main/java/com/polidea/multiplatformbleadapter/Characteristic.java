package com.polidea.multiplatformbleadapter;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.support.annotation.NonNull;

import com.polidea.multiplatformbleadapter.utils.Constants;
import com.polidea.multiplatformbleadapter.utils.IdGenerator;
import com.polidea.multiplatformbleadapter.utils.IdGeneratorKey;
import com.polidea.rxandroidble.internal.RxBleLog;

import java.util.UUID;

public class Characteristic {

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    private int id;
    private int serviceID;
    private UUID serviceUUID;
    private String deviceID;
    private byte[] value;
    private BluetoothGattCharacteristic gattCharacteristic;

    public void setValue(byte[] value) {
        this.value = value;
    }

    public Characteristic(@NonNull Service service, @NonNull BluetoothGattCharacteristic gattCharacteristic) {
        this.deviceID = service.getDeviceID();
        this.serviceUUID = service.getUuid();
        this.serviceID = service.getId();
        this.gattCharacteristic = gattCharacteristic;
        this.id = IdGenerator.getIdForKey(new IdGeneratorKey(service.getDeviceID(), gattCharacteristic.getUuid(), gattCharacteristic.getInstanceId()));
    }

    public Characteristic(int id, @NonNull Service service, BluetoothGattCharacteristic gattCharacteristic) {
        this.id = id;
        this.deviceID = service.getDeviceID();
        this.serviceUUID = service.getUuid();
        this.serviceID = service.getId();
        this.gattCharacteristic = gattCharacteristic;
    }

    public int getId() {
        return this.id;
    }

    public UUID getUuid() {
        return gattCharacteristic.getUuid();
    }

    public int getServiceID() {
        return serviceID;
    }

    public UUID getServiceUUID() {
        return serviceUUID;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public BluetoothGattDescriptor getGattDescriptor(UUID uuid) {
        return gattCharacteristic.getDescriptor(uuid);
    }

    public void setWriteType(int writeType) {
        gattCharacteristic.setWriteType(writeType);
    }

    public boolean isReadable() {
        return (gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) != 0;
    }

    public boolean isWritableWithResponse() {
        return (gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0;
    }

    public boolean isWritableWithoutResponse() {
        return (gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0;
    }

    public boolean isNotifiable() {
        return (gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
    }

    public boolean isNotifying() {
        BluetoothGattDescriptor descriptor = gattCharacteristic.getDescriptor(Constants.CLIENT_CHARACTERISTIC_CONFIG_UUID);
        boolean isNotifying = false;
        if (descriptor != null) {
            byte[] descriptorValue = descriptor.getValue();
            if (descriptorValue != null) {
                isNotifying = (descriptorValue[0] & 0x01) != 0;
            }
        }
        return isNotifying;
    }

    public boolean isIndicatable() {
        return (gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0;
    }

    public byte[] getValue() {
        return value;
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    void logValue(String message, byte[] value) {
        if (value == null) {
            value = gattCharacteristic.getValue();
        }
        String hexValue = value != null ? bytesToHex(value) : "(null)";
        RxBleLog.v(message +
                " Characteristic(uuid: " + gattCharacteristic.getUuid().toString() +
                ", id: " + id +
                ", value: " + hexValue + ")");
    }
}
