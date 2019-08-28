package com.polidea.multiplatformbleadapter;

import android.bluetooth.BluetoothDevice;

public class ScanResult {

    private String rssi;
    private String mtu;
    private BluetoothDevice bluetoothDevice;
    private AdvertisementData advertisementData;

    public ScanResult(String rssi,
                      String mtu,
                      BluetoothDevice bluetoothDevice,
                      AdvertisementData advertisementData) {
        this.bluetoothDevice = bluetoothDevice;
        this.rssi = rssi;
        this.mtu = mtu;
        this.advertisementData = advertisementData;
    }

    public String getRssi() {
        return rssi;
    }

    public String getMtu() {
        return mtu;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public AdvertisementData getAdvertisementData() {
        return advertisementData;
    }
}
