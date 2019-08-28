package com.polidea.multiplatformbleadapter;

interface BleAdapter {

    void createClient(String restoreIdentifierKey);

    void destroyClient();

    void enable(String transactionId, Callback<Void> callback);

    void disable(String transactionId);

    int getCurrentState();

    void startDeviceScan(String[] filteredUUIDs, int scanMode, int callbackType);

    void stopDeviceScan();

    void requestConnectionPriorityForDevice(
            String deviceIdentifier,
            int connectionPriority,
            String transactionId,
            Callback<BleDevice> callback);

    void readRSSIForDevice(
            String deviceIdentifier,
            String transactionId,
            Callback<BleDevice> callback);

    void requestMTUForDevice(
            String deviceIdentifier,
            int mtu,
            String transactionId,
            Callback<BleDevice> callback);

    void getKnownDevices(String[] deviceIdentifiers, Callback<BleDevice[]> callback);

    void getConnectedDevices(String[] serviceUUIDs, Callback<BleDevice> callback);

    void connectToDevice(
            String deviceIdentifier,
            ConnectionOptions connectionOptions,
            Callback<BleDevice> callback);

    void cancelDeviceConnection(String deviceIdentifier, Callback<BleDevice> callback);

    void isDeviceConnected(String deviceIdentifier, Callback<BleDevice> callback);

    void discoverAllServicesAndCharacteristicsForDevice(
            String deviceIdentifier,
            String transactionId,
            Callback<BleDevice> callback);

    void getServicesForDevice(String deviceIdentifier, Callback<Service[]> callback);

    void getCharacteristicsForDevice(
            String deviceIdentifier,
            String serviceUUID,
            Callback<Characteristic[]> callback);

    void getCharacteristicsForService(
            String serviceIdentifier,
            Callback<Characteristic[]> callback);

    void readCharacteristicForDevice(
            String deviceIdentifier,
            String serviceUUID,
            String characteristicUUID,
            String transactionId,
            Callback<Characteristic> callback);

    void readCharacteristicForService(
            int serviceIdentifier,
            String characteristicUUID,
            String transactionId,
            Callback<Characteristic> callback);

    void readCharacteristic(
            int characteristicIdentifer,
            String transactionId,
            Callback<Characteristic> callback);

    void writeCharacteristicForDevice(
            String deviceIdentifier,
            String serviceUUID,
            String characteristicUUID,
            String valueBase64,
            boolean withResponse,
            String transactionId,
            Callback<Characteristic> callback);

    void writeCharacteristicForService(
            String serviceIdentifier,
            String characteristicUUID,
            String valueBase64,
            boolean withResponse,
            String transactionId,
            Callback<Characteristic> callback);

    void writeCharacteristic(
            String characteristicIdentifier,
            String valueBase64,
            boolean withResponse,
            String transactionId,
            Callback<Characteristic> callback);

    void monitorCharacteristicForDevice(
            String deviceIdentifier,
            String serviceUUID,
            String characteristicUUID,
            String transactionId,
            Callback<Void> callback);

    void monitorCharacteristicForService(
            String serviceIdentifier,
            String characteristicUUID,
            String transactionId,
            Callback<Void> callback);

    void monitorCharacteristic(
            String characteristicIdentifier,
            String transactionId,
            Callback<Characteristic> callback);

    void cancelTransaction(String transactionId);

    void setLogLevel(String logLevel);

    void getLogLevel(Callback<String> callback);
}