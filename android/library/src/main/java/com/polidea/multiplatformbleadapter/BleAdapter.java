package com.polidea.multiplatformbleadapter;

interface BleAdapter {

    void createClient(String restoreIdentifierKey);

    void destroyClient();

    void enable(
            String transactionId,
            OnSuccessCallback<Void> onSuccessCallback,
            OnErrorCallback onErrorCallback);

    void disable(String transactionId);

    int getCurrentState();

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
            OnSuccessCallback<BleDevice> onSuccessCallback,
            OnErrorCallback onErrorCallback);

    void readRSSIForDevice(
            String deviceIdentifier,
            String transactionId,
            OnSuccessCallback<BleDevice> onSuccessCallback,
            OnErrorCallback onErrorCallback);

    void requestMTUForDevice(
            String deviceIdentifier,
            int mtu,
            String transactionId,
            OnSuccessCallback<BleDevice> onSuccessCallback,
            OnErrorCallback onErrorCallback);

    void getKnownDevices(
            String[] deviceIdentifiers,
            OnSuccessCallback<BleDevice[]> onSuccessCallback,
            OnErrorCallback onErrorCallback);

    void getConnectedDevices(
            String[] serviceUUIDs,
            OnSuccessCallback<BleDevice> onSuccessCallback,
            OnErrorCallback onErrorCallback);

    void connectToDevice(
            String deviceIdentifier,
            ConnectionOptions connectionOptions,
            OnSuccessCallback<BleDevice> onSuccessCallback,
            OnErrorCallback onErrorCallback);

    void cancelDeviceConnection(
            String deviceIdentifier,
            OnSuccessCallback<BleDevice> onSuccessCallback,
            OnErrorCallback onErrorCallback);

    void isDeviceConnected(
            String deviceIdentifier,
            OnSuccessCallback<BleDevice> onSuccessCallback,
            OnErrorCallback onErrorCallback);

    void discoverAllServicesAndCharacteristicsForDevice(
            String deviceIdentifier,
            String transactionId,
            OnSuccessCallback<BleDevice> onSuccessCallback,
            OnErrorCallback onErrorCallback);

    void getServicesForDevice(
            String deviceIdentifier,
            OnSuccessCallback<Service[]> onSuccessCallback,
            OnErrorCallback onErrorCallback);

    void getCharacteristicsForDevice(
            String deviceIdentifier,
            String serviceUUID,
            OnSuccessCallback<Characteristic[]> onSuccessCallback,
            OnErrorCallback onErrorCallback);

    void getCharacteristicsForService(
            String serviceIdentifier,
            OnSuccessCallback<Characteristic[]> onSuccessCallback,
            OnErrorCallback onErrorCallback);

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
            String serviceIdentifier,
            String characteristicUUID,
            String valueBase64,
            boolean withResponse,
            String transactionId,
            OnSuccessCallback<Characteristic> onSuccessCallback,
            OnErrorCallback onErrorCallback);

    void writeCharacteristic(
            String characteristicIdentifier,
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
            String serviceIdentifier,
            String characteristicUUID,
            String transactionId,
            OnEventCallback<Characteristic> onEventCallback,
            OnErrorCallback onErrorCallback);

    void monitorCharacteristic(
            String characteristicIdentifier,
            String transactionId,
            OnEventCallback<Characteristic> onEventCallback,
            OnErrorCallback onErrorCallback);

    void cancelTransaction(String transactionId);

    void setLogLevel(String logLevel);

    void getLogLevel(OnSuccessCallback<String> onSuccessCallback, OnErrorCallback onErrorCallback);
}