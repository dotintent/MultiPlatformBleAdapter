package com.polidea.multiplatformbleadapter;

import com.polidea.multiplatformbleadapter.errors.BleError;

public interface BleAdapter {

    void createClient(String restoreStateIdentifier,
                      OnEventCallback<String> onAdapterStateChangeCallback,
                      OnEventCallback<Integer> onStateRestored);

    void destroyClient();

    void enable(
            String transactionId,
            OnSuccessCallback<Void> onSuccessCallback,
            OnErrorCallback onErrorCallback);

    void disable(
            String transactionId,
            OnSuccessCallback<Void> onSuccessCallback,
            OnErrorCallback onErrorCallback);

    String getCurrentState();

    void startDeviceScan(
            String[] filteredUUIDs,
            int scanMode,
            int callbackType,
            OnEventCallback<ScanResult> onEventCallback,
            OnErrorCallback onErrorCallback);

    void stopDeviceScan();

    void requestConnectionPriorityForDevice(
            String deviceIdentifier,
            int connectionPriority,
            String transactionId,
            OnSuccessCallback<Device> onSuccessCallback,
            OnErrorCallback onErrorCallback);

    void readRSSIForDevice(
            String deviceIdentifier,
            String transactionId,
            OnSuccessCallback<Device> onSuccessCallback,
            OnErrorCallback onErrorCallback);

    void requestMTUForDevice(
            String deviceIdentifier,
            int mtu,
            String transactionId,
            OnSuccessCallback<Device> onSuccessCallback,
            OnErrorCallback onErrorCallback);

    void getKnownDevices(
            String[] deviceIdentifiers,
            OnSuccessCallback<Device[]> onSuccessCallback,
            OnErrorCallback onErrorCallback);

    void getConnectedDevices(
            String[] serviceUUIDs,
            OnSuccessCallback<Device[]> onSuccessCallback,
            OnErrorCallback onErrorCallback);

    void connectToDevice(
            String deviceIdentifier,
            ConnectionOptions connectionOptions,
            OnSuccessCallback<Device> onSuccessCallback,
            OnEventCallback<ConnectionState> onConnectionStateChangedCallback,
            OnErrorCallback onErrorCallback);

    void cancelDeviceConnection(
            String deviceIdentifier,
            OnSuccessCallback<Device> onSuccessCallback,
            OnErrorCallback onErrorCallback);

    void isDeviceConnected(
            String deviceIdentifier,
            OnSuccessCallback<Boolean> onSuccessCallback,
            OnErrorCallback onErrorCallback);

    void discoverAllServicesAndCharacteristicsForDevice(
            String deviceIdentifier,
            String transactionId,
            OnSuccessCallback<Device> onSuccessCallback,
            OnErrorCallback onErrorCallback);

    Service[] getServicesForDevice(String deviceIdentifier) throws BleError;

    Characteristic[] getCharacteristicsForDevice(
            String deviceIdentifier,
            String serviceUUID) throws BleError;

    Characteristic[] getCharacteristicsForService(int serviceIdentifier) throws BleError;

    void readCharacteristicForDevice(
            String deviceIdentifier,
            String serviceUUID,
            String characteristicUUID,
            String transactionId,
            OnSuccessCallback<Characteristic> onSuccessCallback,
            OnErrorCallback onErrorCallback);

    void readCharacteristicForService(
            int serviceIdentifier,
            String characteristicUUID,
            String transactionId,
            OnSuccessCallback<Characteristic> onSuccessCallback,
            OnErrorCallback onErrorCallback);

    void readCharacteristic(
            int characteristicIdentifer,
            String transactionId,
            OnSuccessCallback<Characteristic> onSuccessCallback,
            OnErrorCallback onErrorCallback);

    void writeCharacteristicForDevice(
            String deviceIdentifier,
            String serviceUUID,
            String characteristicUUID,
            String valueBase64,
            boolean withResponse,
            String transactionId,
            OnSuccessCallback<Characteristic> onSuccessCallback,
            OnErrorCallback onErrorCallback);

    void writeCharacteristicForService(
            int serviceIdentifier,
            String characteristicUUID,
            String valueBase64,
            boolean withResponse,
            String transactionId,
            OnSuccessCallback<Characteristic> onSuccessCallback,
            OnErrorCallback onErrorCallback);

    void writeCharacteristic(
            int characteristicIdentifier,
            String valueBase64,
            boolean withResponse,
            String transactionId,
            OnSuccessCallback<Characteristic> onSuccessCallback,
            OnErrorCallback onErrorCallback);

    void monitorCharacteristicForDevice(
            String deviceIdentifier,
            String serviceUUID,
            String characteristicUUID,
            String transactionId,
            OnEventCallback<Characteristic> onEventCallback,
            OnErrorCallback onErrorCallback);

    void monitorCharacteristicForService(
            int serviceIdentifier,
            String characteristicUUID,
            String transactionId,
            OnEventCallback<Characteristic> onEventCallback,
            OnErrorCallback onErrorCallback);

    void monitorCharacteristic(
            int characteristicIdentifier,
            String transactionId,
            OnEventCallback<Characteristic> onEventCallback,
            OnErrorCallback onErrorCallback);

    void cancelTransaction(String transactionId);

    void setLogLevel(String logLevel);

    String getLogLevel();
}