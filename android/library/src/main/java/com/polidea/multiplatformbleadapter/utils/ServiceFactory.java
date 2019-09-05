package com.polidea.multiplatformbleadapter.utils;

import android.bluetooth.BluetoothGattService;

import com.polidea.multiplatformbleadapter.Service;

public class ServiceFactory {

    public Service create(String deviceId, BluetoothGattService btGattService) {
        return new Service(
                IdGenerator.getIdForKey(new IdGeneratorKey(deviceId, btGattService.getUuid(), btGattService.getInstanceId())),
                deviceId,
                btGattService
        );
    }
}
