package com.polidea.multiplatformbleadapter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.SparseArray;

import com.polidea.multiplatformbleadapter.errors.BleError;
import com.polidea.multiplatformbleadapter.errors.BleErrorCode;
import com.polidea.multiplatformbleadapter.errors.BleErrorUtils;
import com.polidea.multiplatformbleadapter.errors.ErrorConverter;
import com.polidea.multiplatformbleadapter.exceptions.CannotMonitorCharacteristicException;
import com.polidea.multiplatformbleadapter.utils.Base64Converter;
import com.polidea.multiplatformbleadapter.utils.Constants;
import com.polidea.multiplatformbleadapter.utils.DisposableMap;
import com.polidea.multiplatformbleadapter.utils.IdGenerator;
import com.polidea.multiplatformbleadapter.utils.LogLevel;
import com.polidea.multiplatformbleadapter.utils.OneTimeActionExecutor;
import com.polidea.multiplatformbleadapter.utils.RefreshGattCustomOperation;
import com.polidea.multiplatformbleadapter.utils.ServiceFactory;
import com.polidea.multiplatformbleadapter.utils.UUIDConverter;
import com.polidea.multiplatformbleadapter.utils.mapper.RxBleDeviceToDeviceMapper;
import com.polidea.multiplatformbleadapter.utils.mapper.RxScanResultToScanResultMapper;
import com.polidea.rxandroidble.NotificationSetupMode;
import com.polidea.rxandroidble.RxBleAdapterStateObservable;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.RxBleDeviceServices;
import com.polidea.rxandroidble.exceptions.BleCharacteristicNotFoundException;
import com.polidea.rxandroidble.internal.RxBleLog;
import com.polidea.rxandroidble.scan.ScanFilter;
import com.polidea.rxandroidble.scan.ScanSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;

import static com.polidea.multiplatformbleadapter.utils.Constants.BluetoothState;

public class BleModule implements BleAdapter {

    private final ErrorConverter errorConverter = new ErrorConverter();

    @Nullable
    private RxBleClient rxBleClient;

    private HashMap<String, Device> discoveredDevices = new HashMap<>();

    private HashMap<String, Device> connectedDevices = new HashMap<>();

    private HashMap<String, RxBleConnection> activeConnections = new HashMap<>();

    private SparseArray<Service> discoveredServices = new SparseArray<>();

    private SparseArray<Characteristic> discoveredCharacteristics = new SparseArray<>();

    private final DisposableMap pendingTransactions = new DisposableMap();

    private final DisposableMap connectingDevices = new DisposableMap();

    private BluetoothManager bluetoothManager;

    private BluetoothAdapter bluetoothAdapter;

    private Context context;

    @Nullable
    private Subscription scanSubscription;

    @Nullable
    private Subscription adapterStateChangesSubscription;

    private RxBleDeviceToDeviceMapper rxBleDeviceToDeviceMapper = new RxBleDeviceToDeviceMapper();

    private RxScanResultToScanResultMapper rxScanResultToScanResultMapper = new RxScanResultToScanResultMapper();

    private ServiceFactory serviceFactory = new ServiceFactory();

    private int currentLogLevel = RxBleLog.NONE;

    public BleModule(Context context) {
        this.context = context;
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    @Override
    public void createClient(String restoreStateIdentifier,
                             OnEventCallback<String> onAdapterStateChangeCallback,
                             OnEventCallback<Integer> onStateRestored) {
        rxBleClient = RxBleClient.create(context);
        adapterStateChangesSubscription = monitorAdapterStateChanges(context, onAdapterStateChangeCallback);

        // We need to send signal that BLE Module starts without restored state
        if (restoreStateIdentifier != null) {
            onStateRestored.onEvent(null);
        }
    }

    @Override
    public void destroyClient() {
        if (adapterStateChangesSubscription != null) {
            adapterStateChangesSubscription.unsubscribe();
            adapterStateChangesSubscription = null;
        }
        if (scanSubscription != null && !scanSubscription.isUnsubscribed()) {
            scanSubscription.unsubscribe();
            scanSubscription = null;
        }
        pendingTransactions.removeAllSubscriptions();
        connectingDevices.removeAllSubscriptions();

        discoveredServices.clear();
        discoveredCharacteristics.clear();
        connectedDevices.clear();
        activeConnections.clear();
        discoveredDevices.clear();

        rxBleClient = null;
        IdGenerator.clear();
    }


    @Override
    public void enable(final String transactionId,
                       final OnSuccessCallback<Void> onSuccessCallback,
                       final OnErrorCallback onErrorCallback) {
        changeAdapterState(
                RxBleAdapterStateObservable.BleAdapterState.STATE_ON,
                transactionId,
                onSuccessCallback,
                onErrorCallback);
    }

    @Override
    public void disable(final String transactionId,
                        final OnSuccessCallback<Void> onSuccessCallback,
                        final OnErrorCallback onErrorCallback) {
        changeAdapterState(
                RxBleAdapterStateObservable.BleAdapterState.STATE_OFF,
                transactionId,
                onSuccessCallback,
                onErrorCallback);
    }

    @BluetoothState
    @Override
    public String getCurrentState() {
        if (!supportsBluetoothLowEnergy()) return BluetoothState.UNSUPPORTED;
        if (bluetoothManager == null) return BluetoothState.POWERED_OFF;
        return mapNativeAdapterStateToLocalBluetoothState(bluetoothAdapter.getState());
    }

    @Override
    public void startDeviceScan(String[] filteredUUIDs,
                                int scanMode,
                                int callbackType,
                                OnEventCallback<ScanResult> onEventCallback,
                                OnErrorCallback onErrorCallback) {
        UUID[] uuids = null;

        if (filteredUUIDs != null) {
            uuids = UUIDConverter.convert(filteredUUIDs);
            if (uuids == null) {
                onErrorCallback.onError(BleErrorUtils.invalidIdentifiers(filteredUUIDs));
                return;
            }
        }

        safeStartDeviceScan(uuids, scanMode, callbackType, onEventCallback, onErrorCallback);
    }

    @Override
    public void stopDeviceScan() {
        if (scanSubscription != null) {
            scanSubscription.unsubscribe();
            scanSubscription = null;
        }
    }

    @Override
    public void requestConnectionPriorityForDevice(String deviceIdentifier,
                                                   int connectionPriority,
                                                   final String transactionId,
                                                   final OnSuccessCallback<Device> onSuccessCallback,
                                                   final OnErrorCallback onErrorCallback) {
        final Device device;
        try {
            device = getDeviceById(deviceIdentifier);
        } catch (BleError error) {
            onErrorCallback.onError(error);
            return;
        }

        final RxBleConnection connection = getConnectionOrEmitError(device.getId(), onErrorCallback);
        if (connection == null) {
            return;
        }

        final OneTimeActionExecutor<BleError> oneTimeErrorCallback = new OneTimeActionExecutor<BleError>() {
            @Override
            public void action(BleError error) {
                onErrorCallback.onError(error);
            }
        };

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            final Subscription subscription = connection
                    .requestConnectionPriority(connectionPriority, 1, TimeUnit.MILLISECONDS)
                    .doOnUnsubscribe(new Action0() {
                        @Override
                        public void call() {
                            oneTimeErrorCallback.execute(BleErrorUtils.cancelled());
                            pendingTransactions.removeSubscription(transactionId);
                        }
                    }).subscribe(new Action0() {
                        @Override
                        public void call() {
                            onSuccessCallback.onSuccess(device);
                            pendingTransactions.removeSubscription(transactionId);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable error) {
                            oneTimeErrorCallback.execute(errorConverter.toError(error));
                            pendingTransactions.removeSubscription(transactionId);
                        }
                    });

            pendingTransactions.replaceSubscription(transactionId, subscription);
        } else {
            onSuccessCallback.onSuccess(device);
        }
    }

    @Override
    public void readRSSIForDevice(String deviceIdentifier,
                                  final String transactionId,
                                  final OnSuccessCallback<Device> onSuccessCallback,
                                  final OnErrorCallback onErrorCallback) {
        final Device device;
        try {
            device = getDeviceById(deviceIdentifier);
        } catch (BleError error) {
            onErrorCallback.onError(error);
            return;
        }
        final RxBleConnection connection = getConnectionOrEmitError(device.getId(), onErrorCallback);
        if (connection == null) {
            return;
        }

        final OneTimeActionExecutor<BleError> oneTimeErrorCallback = new OneTimeActionExecutor<BleError>() {
            @Override
            public void action(BleError error) {
                onErrorCallback.onError(error);
            }
        };

        final Subscription subscription = connection
                .readRssi()
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        oneTimeErrorCallback.execute(BleErrorUtils.cancelled());
                        pendingTransactions.removeSubscription(transactionId);
                    }
                })
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {
                        pendingTransactions.removeSubscription(transactionId);
                    }

                    @Override
                    public void onError(Throwable error) {
                        oneTimeErrorCallback.execute(errorConverter.toError(error));
                        pendingTransactions.removeSubscription(transactionId);
                    }

                    @Override
                    public void onNext(Integer rssi) {
                        device.setRssi(rssi);
                        onSuccessCallback.onSuccess(device);
                    }
                });

        pendingTransactions.replaceSubscription(transactionId, subscription);
    }

    @Override
    public void requestMTUForDevice(String deviceIdentifier, int mtu,
                                    final String transactionId,
                                    final OnSuccessCallback<Device> onSuccessCallback,
                                    final OnErrorCallback onErrorCallback) {
        final Device device;
        try {
            device = getDeviceById(deviceIdentifier);
        } catch (BleError error) {
            onErrorCallback.onError(error);
            return;
        }

        final RxBleConnection connection = getConnectionOrEmitError(device.getId(), onErrorCallback);
        if (connection == null) {
            return;
        }

        final OneTimeActionExecutor<BleError> oneTimeErrorCallback = new OneTimeActionExecutor<BleError>() {
            @Override
            public void action(BleError error) {
                onErrorCallback.onError(error);
            }
        };

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            final Subscription subscription = connection
                    .requestMtu(mtu)
                    .doOnUnsubscribe(new Action0() {
                        @Override
                        public void call() {
                            oneTimeErrorCallback.execute(BleErrorUtils.cancelled());
                            pendingTransactions.removeSubscription(transactionId);
                        }
                    }).subscribe(new Observer<Integer>() {
                        @Override
                        public void onCompleted() {
                            pendingTransactions.removeSubscription(transactionId);
                        }

                        @Override
                        public void onError(Throwable error) {
                            oneTimeErrorCallback.execute(errorConverter.toError(error));
                            pendingTransactions.removeSubscription(transactionId);
                        }

                        @Override
                        public void onNext(Integer mtu) {
                            device.setMtu(mtu);
                            onSuccessCallback.onSuccess(device);
                        }
                    });

            pendingTransactions.replaceSubscription(transactionId, subscription);
        } else {
            onSuccessCallback.onSuccess(device);
        }
    }

    @Override
    public void getKnownDevices(String[] deviceIdentifiers,
                                OnSuccessCallback<Device[]> onSuccessCallback,
                                OnErrorCallback onErrorCallback) {
        if (rxBleClient == null) {
            throw new IllegalStateException("BleManager not created when tried connecting to device");
        }

        Device[] knownDevices = new Device[deviceIdentifiers.length];
        int knownDevicesCount = 0;
        for (final String deviceId : deviceIdentifiers) {
            if (deviceId == null) {
                onErrorCallback.onError(BleErrorUtils.invalidIdentifiers(deviceIdentifiers));
                return;
            }

            final Device device = discoveredDevices.get(deviceId);
            if (device != null) {
                knownDevices[++knownDevicesCount] = device;
            }
        }

        onSuccessCallback.onSuccess(knownDevices);
    }

    @Override
    public void getConnectedDevices(String[] serviceUUIDs,
                                    OnSuccessCallback<Device[]> onSuccessCallback,
                                    OnErrorCallback onErrorCallback) {
        if (rxBleClient == null) {
            throw new IllegalStateException("BleManager not created when tried connecting to device");
        }

        UUID[] uuids = new UUID[serviceUUIDs.length];
        for (int i = 0; i < serviceUUIDs.length; i++) {
            UUID uuid = UUIDConverter.convert(serviceUUIDs[i]);

            if (uuid == null) {
                onErrorCallback.onError(BleErrorUtils.invalidIdentifiers(serviceUUIDs));
                return;
            }

            uuids[i] = uuid;
        }

        Device[] localConnectedDevices = new Device[connectedDevices.size()];
        int connectedDevicesCount = 0;
        for (Device device : connectedDevices.values()) {
            for (UUID uuid : uuids) {
                if (device.getServiceByUUID(uuid) != null) {
                    localConnectedDevices[connectedDevicesCount++] = device;
                    break;
                }
            }
        }

        onSuccessCallback.onSuccess(localConnectedDevices);
    }

    @Override
    public void connectToDevice(String deviceIdentifier,
                                ConnectionOptions connectionOptions,
                                OnSuccessCallback<Device> onSuccessCallback,
                                OnEventCallback<ConnectionState> onConnectionStateChangedCallback,
                                OnErrorCallback onErrorCallback) {
        if (rxBleClient == null) {
            throw new IllegalStateException("BleManager not created when tried connecting to device");
        }

        final RxBleDevice device = rxBleClient.getBleDevice(deviceIdentifier);
        if (device == null) {
            onErrorCallback.onError(BleErrorUtils.deviceNotFound(deviceIdentifier));
            return;
        }

        safeConnectToDevice(
                device,
                connectionOptions.getAutoConnect(),
                connectionOptions.getRequestMTU(),
                connectionOptions.getRefreshGattMoment(),
                connectionOptions.getTimeoutInMillis(),
                connectionOptions.getConnectionPriority(),
                onSuccessCallback, onConnectionStateChangedCallback, onErrorCallback);
    }

    @Override
    public void cancelDeviceConnection(String deviceIdentifier,
                                       OnSuccessCallback<Device> onSuccessCallback,
                                       OnErrorCallback onErrorCallback) {
        if (rxBleClient == null) {
            throw new IllegalStateException("BleManager not created when tried cancel device connection");
        }

        final RxBleDevice device = rxBleClient.getBleDevice(deviceIdentifier);

        if (connectingDevices.removeSubscription(deviceIdentifier) && device != null) {
            onSuccessCallback.onSuccess(rxBleDeviceToDeviceMapper.map(device));
        } else {
            if (device == null) {
                onErrorCallback.onError(BleErrorUtils.deviceNotFound(deviceIdentifier));
            } else {
                onErrorCallback.onError(BleErrorUtils.deviceNotConnected(deviceIdentifier));
            }
        }
    }

    @Override
    public void isDeviceConnected(String deviceIdentifier,
                                  OnSuccessCallback<Boolean> onSuccessCallback,
                                  OnErrorCallback onErrorCallback) {
        if (rxBleClient == null) {
            throw new IllegalStateException("BleManager not created when tried cancel device connection");
        }

        final RxBleDevice device = rxBleClient.getBleDevice(deviceIdentifier);
        if (device == null) {
            onErrorCallback.onError(BleErrorUtils.deviceNotFound(deviceIdentifier));
            return;
        }

        boolean connected = device.getConnectionState()
                .equals(RxBleConnection.RxBleConnectionState.CONNECTED);
        onSuccessCallback.onSuccess(connected);
    }

    @Override
    public void discoverAllServicesAndCharacteristicsForDevice(String deviceIdentifier,
                                                               String transactionId,
                                                               OnSuccessCallback<Device> onSuccessCallback,
                                                               OnErrorCallback onErrorCallback) {
        final Device device;
        try {
            device = getDeviceById(deviceIdentifier);
        } catch (BleError error) {
            onErrorCallback.onError(error);
            return;
        }

        safeDiscoverAllServicesAndCharacteristicsForDevice(device, transactionId, onSuccessCallback, onErrorCallback);
    }

    @Override
    public Service[] getServicesForDevice(String deviceIdentifier) throws BleError {
        final Device device = getDeviceById(deviceIdentifier);
        final List<Service> services = device.getServices();
        if (services == null) {
            throw BleErrorUtils.deviceServicesNotDiscovered(device.getId());
        }
        return services.toArray(new Service[]{});
    }

    @Override
    public Characteristic[] getCharacteristicsForDevice(String deviceIdentifier,
                                                        String serviceUUID) throws BleError {
        final UUID convertedServiceUUID = UUIDConverter.convert(serviceUUID);
        if (convertedServiceUUID == null) {
            throw BleErrorUtils.invalidIdentifiers(serviceUUID);
        }

        final Device device = getDeviceById(deviceIdentifier);

        final Service service = device.getServiceByUUID(convertedServiceUUID);
        if (service == null) {
            throw BleErrorUtils.serviceNotFound(serviceUUID);
        }

        return service.getCharacteristics().toArray(new Characteristic[]{});
    }

    @Override
    public Characteristic[] getCharacteristicsForService(int serviceIdentifier) throws BleError {
        Service service = discoveredServices.get(serviceIdentifier);
        if (service == null) {
            throw BleErrorUtils.serviceNotFound(Integer.toString(serviceIdentifier));
        }
        return service.getCharacteristics().toArray(new Characteristic[]{});
    }

    @Override
    public void readCharacteristicForDevice(String deviceIdentifier,
                                            String serviceUUID,
                                            String characteristicUUID,
                                            String transactionId,
                                            OnSuccessCallback<Characteristic> onSuccessCallback,
                                            OnErrorCallback onErrorCallback) {
        final Characteristic characteristic = getCharacteristicOrEmitError(
                deviceIdentifier, serviceUUID, characteristicUUID, onErrorCallback);
        if (characteristic == null) {
            return;
        }

        safeReadCharacteristicForDevice(characteristic, transactionId, onSuccessCallback, onErrorCallback);
    }

    @Override
    public void readCharacteristicForService(int serviceIdentifier,
                                             String characteristicUUID,
                                             String transactionId,
                                             OnSuccessCallback<Characteristic> onSuccessCallback,
                                             OnErrorCallback onErrorCallback) {
        final Characteristic characteristic = getCharacteristicOrEmitError(
                serviceIdentifier, characteristicUUID, onErrorCallback);
        if (characteristic == null) {
            return;
        }

        safeReadCharacteristicForDevice(characteristic, transactionId, onSuccessCallback, onErrorCallback);
    }

    @Override
    public void readCharacteristic(int characteristicIdentifier,
                                   String transactionId,
                                   OnSuccessCallback<Characteristic> onSuccessCallback,
                                   OnErrorCallback onErrorCallback) {
        final Characteristic characteristic = getCharacteristicOrEmitError(characteristicIdentifier, onErrorCallback);
        if (characteristic == null) {
            return;
        }

        safeReadCharacteristicForDevice(characteristic, transactionId, onSuccessCallback, onErrorCallback);
    }

    @Override
    public void writeCharacteristicForDevice(String deviceIdentifier,
                                             String serviceUUID,
                                             String characteristicUUID,
                                             String valueBase64,
                                             boolean withResponse,
                                             String transactionId,
                                             OnSuccessCallback<Characteristic> onSuccessCallback,
                                             OnErrorCallback onErrorCallback) {
        final Characteristic characteristic = getCharacteristicOrEmitError(
                deviceIdentifier, serviceUUID, characteristicUUID, onErrorCallback);
        if (characteristic == null) {
            return;
        }

        writeCharacteristicWithValue(
                characteristic,
                valueBase64,
                withResponse,
                transactionId,
                onSuccessCallback,
                onErrorCallback);
    }

    @Override
    public void writeCharacteristicForService(int serviceIdentifier,
                                              String characteristicUUID,
                                              String valueBase64,
                                              boolean withResponse,
                                              String transactionId,
                                              OnSuccessCallback<Characteristic> onSuccessCallback,
                                              OnErrorCallback onErrorCallback) {
        final Characteristic characteristic = getCharacteristicOrEmitError(
                serviceIdentifier, characteristicUUID, onErrorCallback);
        if (characteristic == null) {
            return;
        }

        writeCharacteristicWithValue(
                characteristic,
                valueBase64,
                withResponse,
                transactionId,
                onSuccessCallback,
                onErrorCallback);
    }

    @Override
    public void writeCharacteristic(int characteristicIdentifier,
                                    String valueBase64,
                                    boolean withResponse,
                                    String transactionId,
                                    OnSuccessCallback<Characteristic> onSuccessCallback,
                                    OnErrorCallback onErrorCallback) {
        final Characteristic characteristic = getCharacteristicOrEmitError(characteristicIdentifier, onErrorCallback);
        if (characteristic == null) {
            return;
        }

        writeCharacteristicWithValue(
                characteristic,
                valueBase64,
                withResponse,
                transactionId,
                onSuccessCallback,
                onErrorCallback
        );
    }

    @Override
    public void monitorCharacteristicForDevice(String deviceIdentifier,
                                               String serviceUUID,
                                               String characteristicUUID,
                                               String transactionId,
                                               OnEventCallback<Characteristic> onEventCallback,
                                               OnErrorCallback onErrorCallback) {
        final Characteristic characteristic = getCharacteristicOrEmitError(
                deviceIdentifier, serviceUUID, characteristicUUID, onErrorCallback);
        if (characteristic == null) {
            return;
        }

        safeMonitorCharacteristicForDevice(characteristic, transactionId, onEventCallback, onErrorCallback);
    }

    @Override
    public void monitorCharacteristicForService(int serviceIdentifier,
                                                String characteristicUUID,
                                                String transactionId,
                                                OnEventCallback<Characteristic> onEventCallback,
                                                OnErrorCallback onErrorCallback) {
        final Characteristic characteristic = getCharacteristicOrEmitError(
                serviceIdentifier, characteristicUUID, onErrorCallback);
        if (characteristic == null) {
            return;
        }

        safeMonitorCharacteristicForDevice(characteristic, transactionId, onEventCallback, onErrorCallback);
    }

    @Override
    public void monitorCharacteristic(int characteristicIdentifier, String transactionId,
                                      OnEventCallback<Characteristic> onEventCallback,
                                      OnErrorCallback onErrorCallback) {
        final Characteristic characteristic = getCharacteristicOrEmitError(characteristicIdentifier, onErrorCallback);
        if (characteristic == null) {
            return;
        }

        safeMonitorCharacteristicForDevice(characteristic, transactionId, onEventCallback, onErrorCallback);
    }

    @Override
    public void cancelTransaction(String transactionId) {
        pendingTransactions.removeSubscription(transactionId);
    }

    public void setLogLevel(String logLevel) {
        currentLogLevel = LogLevel.toLogLevel(logLevel);
        RxBleLog.setLogLevel(currentLogLevel);
    }

    @Override
    public String getLogLevel() {
        return LogLevel.fromLogLevel(currentLogLevel);
    }

    private Subscription monitorAdapterStateChanges(Context context,
                                                    final OnEventCallback<String> onAdapterStateChangeCallback) {
        if (!supportsBluetoothLowEnergy()) {
            return null;
        }

        return new RxBleAdapterStateObservable(context)
                .map(new Func1<RxBleAdapterStateObservable.BleAdapterState, String>() {
                    @Override
                    public String call(RxBleAdapterStateObservable.BleAdapterState bleAdapterState) {
                        return mapRxBleAdapterStateToLocalBluetoothState(bleAdapterState);
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String state) {
                        onAdapterStateChangeCallback.onEvent(state);
                    }
                });
    }

    private boolean supportsBluetoothLowEnergy() {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    @BluetoothState
    private String mapRxBleAdapterStateToLocalBluetoothState(
            RxBleAdapterStateObservable.BleAdapterState rxBleAdapterState
    ) {
        if (rxBleAdapterState == RxBleAdapterStateObservable.BleAdapterState.STATE_ON) {
            return BluetoothState.POWERED_ON;
        } else if (rxBleAdapterState == RxBleAdapterStateObservable.BleAdapterState.STATE_OFF) {
            return BluetoothState.POWERED_OFF;
        } else {
            return BluetoothState.RESETTING;
        }
    }

    private void changeAdapterState(final RxBleAdapterStateObservable.BleAdapterState desiredAdapterState,
                                    final String transactionId,
                                    final OnSuccessCallback<Void> onSuccessCallback,
                                    final OnErrorCallback onErrorCallback) {
        if (bluetoothManager == null) {
            onErrorCallback.onError(new BleError(BleErrorCode.BluetoothStateChangeFailed, "BluetoothManager is null", null));
            return;
        }

        final OneTimeActionExecutor<BleError> oneTimeErrorCallback = new OneTimeActionExecutor<BleError>() {
            @Override
            public void action(BleError error) {
                onErrorCallback.onError(error);
            }
        };

        final Subscription subscription = new RxBleAdapterStateObservable(context)
                .takeUntil(new Func1<RxBleAdapterStateObservable.BleAdapterState, Boolean>() {
                    @Override
                    public Boolean call(RxBleAdapterStateObservable.BleAdapterState actualAdapterState) {
                        return desiredAdapterState == actualAdapterState;
                    }
                })
                .toCompletable()
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        oneTimeErrorCallback.execute(BleErrorUtils.cancelled());
                        pendingTransactions.removeSubscription(transactionId);
                    }
                })
                .subscribe(new Action0() {
                    @Override
                    public void call() {
                        onSuccessCallback.onSuccess(null);
                        pendingTransactions.removeSubscription(transactionId);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable error) {
                        oneTimeErrorCallback.execute(errorConverter.toError(error));
                        pendingTransactions.removeSubscription(transactionId);
                    }
                });

        if (!bluetoothAdapter.enable()) {
            subscription.unsubscribe();
            onErrorCallback.onError(new BleError(
                    BleErrorCode.BluetoothStateChangeFailed,
                    String.format("Couldn't set bluetooth adapter state to %s", desiredAdapterState.toString()),
                    null));
        } else {
            pendingTransactions.replaceSubscription(transactionId, subscription);
        }
    }

    @BluetoothState
    private String mapNativeAdapterStateToLocalBluetoothState(int adapterState) {
        switch (adapterState) {
            case BluetoothAdapter.STATE_OFF:
                return BluetoothState.POWERED_OFF;
            case BluetoothAdapter.STATE_ON:
                return BluetoothState.POWERED_ON;
            case BluetoothAdapter.STATE_TURNING_OFF:
                // fallthrough
            case BluetoothAdapter.STATE_TURNING_ON:
                return BluetoothState.RESETTING;
            default:
                return BluetoothState.UNKNOWN;
        }
    }

    private void safeStartDeviceScan(final UUID[] uuids,
                                     final int scanMode,
                                     int callbackType,
                                     final OnEventCallback<ScanResult> onEventCallback,
                                     final OnErrorCallback onErrorCallback) {
        if (rxBleClient == null) {
            throw new IllegalStateException("BleManager not created when tried to start device scan");
        }

        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(scanMode)
                .setCallbackType(callbackType)
                .build();

        int length = uuids == null ? 0 : uuids.length;
        ScanFilter[] filters = new ScanFilter[length];
        for (int i = 0; i < length; i++) {
            filters[i] = new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(uuids[i].toString())).build();
        }

        scanSubscription = rxBleClient
                .scanBleDevices(scanSettings, filters)
                .subscribe(new Action1<com.polidea.rxandroidble.scan.ScanResult>() {
                    @Override
                    public void call(com.polidea.rxandroidble.scan.ScanResult scanResult) {
                        String deviceId = scanResult.getBleDevice().getMacAddress();
                        if (!discoveredDevices.containsKey(deviceId)) {
                            discoveredDevices.put(deviceId, rxBleDeviceToDeviceMapper.map(scanResult.getBleDevice()));
                        }
                        onEventCallback.onEvent(rxScanResultToScanResultMapper.map(scanResult));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        onErrorCallback.onError(errorConverter.toError(throwable));
                    }
                });
    }

    @NonNull
    private Device getDeviceById(@NonNull final String deviceId) throws BleError {
        final Device device = connectedDevices.get(deviceId);
        if (device == null) {
            throw BleErrorUtils.deviceNotConnected(deviceId);
        }
        return device;
    }

    @Nullable
    private RxBleConnection getConnectionOrEmitError(@NonNull final String deviceId,
                                                     @NonNull OnErrorCallback onErrorCallback) {
        final RxBleConnection connection = activeConnections.get(deviceId);
        if (connection == null) {
            onErrorCallback.onError(BleErrorUtils.deviceNotConnected(deviceId));
            return null;
        }
        return connection;
    }

    private void safeConnectToDevice(final RxBleDevice device,
                                     final boolean autoConnect,
                                     final int requestMtu,
                                     final RefreshGattMoment refreshGattMoment,
                                     final Long timeout,
                                     final int connectionPriority,
                                     final OnSuccessCallback<Device> onSuccessCallback,
                                     final OnEventCallback<ConnectionState> onConnectionStateChangedCallback,
                                     final OnErrorCallback onErrorCallback) {

        final OneTimeActionExecutor<BleError> oneTimeErrorCallback = new OneTimeActionExecutor<BleError>() {
            @Override
            public void action(BleError error) {
                onErrorCallback.onError(error);
            }
        };

        Observable<RxBleConnection> connect = device
                .establishConnection(autoConnect)
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        onConnectionStateChangedCallback.onEvent(ConnectionState.CONNECTING);
                    }
                })
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        oneTimeErrorCallback.execute(BleErrorUtils.cancelled());
                        onDeviceDisconnected(device);
                        onConnectionStateChangedCallback.onEvent(ConnectionState.DISCONNECTED);
                    }
                });

        if (refreshGattMoment == RefreshGattMoment.ON_CONNECTED) {
            connect = connect.flatMap(new Func1<RxBleConnection, Observable<RxBleConnection>>() {
                @Override
                public Observable<RxBleConnection> call(final RxBleConnection rxBleConnection) {
                    return rxBleConnection
                            .queue(new RefreshGattCustomOperation())
                            .map(new Func1<Boolean, RxBleConnection>() {
                                @Override
                                public RxBleConnection call(Boolean refreshGattSuccess) {
                                    return rxBleConnection;
                                }
                            });
                }
            });
        }

        if (connectionPriority > 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connect = connect.flatMap(new Func1<RxBleConnection, Observable<RxBleConnection>>() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public Observable<RxBleConnection> call(final RxBleConnection rxBleConnection) {
                    return rxBleConnection
                            .requestConnectionPriority(connectionPriority, 1, TimeUnit.MILLISECONDS)
                            .andThen(Observable.just(rxBleConnection));
                }
            });
        }

        if (requestMtu > 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connect = connect.flatMap(new Func1<RxBleConnection, Observable<RxBleConnection>>() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public Observable<RxBleConnection> call(final RxBleConnection rxBleConnection) {
                    return rxBleConnection
                            .requestMtu(requestMtu)
                            .map(new Func1<Integer, RxBleConnection>() {
                                @Override
                                public RxBleConnection call(Integer integer) {
                                    return rxBleConnection;
                                }
                            });
                }
            });
        }

        if (timeout != null) {
            connect = connect.timeout(new Func0<Observable<Long>>() {
                @Override
                public Observable<Long> call() {
                    return Observable.timer(timeout, TimeUnit.MILLISECONDS);
                }
            }, new Func1<RxBleConnection, Observable<Long>>() {
                @Override
                public Observable<Long> call(RxBleConnection rxBleConnection) {
                    return Observable.never();
                }
            });
        }


        final Subscription subscription = connect
                .subscribe(new Observer<RxBleConnection>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        BleError bleError = errorConverter.toError(e);
                        oneTimeErrorCallback.execute(bleError);
                        onDeviceDisconnected(device);
                    }

                    @Override
                    public void onNext(RxBleConnection connection) {
                        Device localDevice = rxBleDeviceToDeviceMapper.map(device);
                        onConnectionStateChangedCallback.onEvent(ConnectionState.CONNECTED);
                        cleanServicesAndCharacteristicsForDevice(localDevice);
                        connectedDevices.put(device.getMacAddress(), localDevice);
                        activeConnections.put(device.getMacAddress(), connection);
                        onSuccessCallback.onSuccess(localDevice);
                    }
                });

        connectingDevices.replaceSubscription(device.getMacAddress(), subscription);
    }

    private void onDeviceDisconnected(RxBleDevice rxDevice) {
        activeConnections.remove(rxDevice.getMacAddress());
        Device device = connectedDevices.remove(rxDevice.getMacAddress());
        if (device == null) {
            return;
        }

        cleanServicesAndCharacteristicsForDevice(device);
        connectingDevices.removeSubscription(device.getId());
    }

    private void safeDiscoverAllServicesAndCharacteristicsForDevice(final Device device,
                                                                    final String transactionId,
                                                                    final OnSuccessCallback<Device> onSuccessCallback,
                                                                    final OnErrorCallback onErrorCallback) {
        final RxBleConnection connection = getConnectionOrEmitError(device.getId(), onErrorCallback);
        if (connection == null) {
            return;
        }

        final OneTimeActionExecutor<BleError> oneTimeErrorCallback = new OneTimeActionExecutor<BleError>() {
            @Override
            public void action(BleError error) {
                onErrorCallback.onError(error);
            }
        };

        final Subscription subscription = connection
                .discoverServices()
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        oneTimeErrorCallback.execute(BleErrorUtils.cancelled());
                        pendingTransactions.removeSubscription(transactionId);
                    }
                })
                .subscribe(new Observer<RxBleDeviceServices>() {
                    @Override
                    public void onCompleted() {
                        onSuccessCallback.onSuccess(device);
                        pendingTransactions.removeSubscription(transactionId);
                    }

                    @Override
                    public void onError(Throwable error) {
                        oneTimeErrorCallback.execute(errorConverter.toError(error));
                        pendingTransactions.removeSubscription(transactionId);
                    }

                    @Override
                    public void onNext(RxBleDeviceServices rxBleDeviceServices) {
                        ArrayList<Service> services = new ArrayList<>();
                        for (BluetoothGattService gattService : rxBleDeviceServices.getBluetoothGattServices()) {
                            Service service = serviceFactory.create(device.getId(), gattService);
                            discoveredServices.put(service.getId(), service);
                            services.add(service);

                            for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {
                                Characteristic characteristic = new Characteristic(service, gattCharacteristic);
                                discoveredCharacteristics.put(characteristic.getId(), characteristic);
                            }
                        }
                        device.setServices(services);
                    }
                });

        pendingTransactions.replaceSubscription(transactionId, subscription);
    }

    private void safeReadCharacteristicForDevice(final Characteristic characteristic,
                                                 final String transactionId,
                                                 final OnSuccessCallback<Characteristic> onSuccessCallback,
                                                 final OnErrorCallback onErrorCallback) {
        final RxBleConnection connection = getConnectionOrEmitError(characteristic.getDeviceID(), onErrorCallback);
        if (connection == null) {
            return;
        }

        final OneTimeActionExecutor<BleError> oneTimeErrorCallback = new OneTimeActionExecutor<BleError>() {
            @Override
            public void action(BleError error) {
                onErrorCallback.onError(error);
            }
        };

        final Subscription subscription = connection
                .readCharacteristic(characteristic.getUuid())
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        oneTimeErrorCallback.execute(BleErrorUtils.cancelled());
                        pendingTransactions.removeSubscription(transactionId);
                    }
                })
                .subscribe(new Observer<byte[]>() {
                    @Override
                    public void onCompleted() {
                        pendingTransactions.removeSubscription(transactionId);
                    }

                    @Override
                    public void onError(Throwable error) {
                        if (error instanceof BleCharacteristicNotFoundException) {
                            oneTimeErrorCallback.execute(
                                    BleErrorUtils.characteristicNotFound(
                                            UUIDConverter.fromUUID(characteristic.getUuid())));
                            return;
                        }
                        oneTimeErrorCallback.execute(errorConverter.toError(error));
                        pendingTransactions.removeSubscription(transactionId);
                    }

                    @Override
                    public void onNext(byte[] bytes) {
                        characteristic.logValue("Read from", bytes);
                        characteristic.setValue(bytes);
                        onSuccessCallback.onSuccess(characteristic);
                    }
                });

        pendingTransactions.replaceSubscription(transactionId, subscription);
    }

    private void writeCharacteristicWithValue(final Characteristic characteristic,
                                              final String valueBase64,
                                              final Boolean response,
                                              final String transactionId,
                                              OnSuccessCallback<Characteristic> onSuccessCallback,
                                              OnErrorCallback onErrorCallback) {
        final byte[] value;
        try {
            value = Base64Converter.decode(valueBase64);
        } catch (Throwable error) {
            onErrorCallback.onError(
                    BleErrorUtils.invalidWriteDataForCharacteristic(valueBase64,
                            UUIDConverter.fromUUID(characteristic.getUuid())));
            return;
        }

        characteristic.setWriteType(response ?
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT :
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);

        safeWriteCharacteristicForDevice(
                characteristic,
                value,
                transactionId,
                onSuccessCallback,
                onErrorCallback);
    }

    private void safeWriteCharacteristicForDevice(final Characteristic characteristic,
                                                  final byte[] value,
                                                  final String transactionId,
                                                  final OnSuccessCallback<Characteristic> onSuccessCallback,
                                                  final OnErrorCallback onErrorCallback) {
        final RxBleConnection connection = getConnectionOrEmitError(characteristic.getDeviceID(), onErrorCallback);
        if (connection == null) {
            return;
        }

        final OneTimeActionExecutor<BleError> oneTimeErrorCallback = new OneTimeActionExecutor<BleError>() {
            @Override
            public void action(BleError error) {
                onErrorCallback.onError(error);
            }
        };

        final Subscription subscription = connection
                .writeCharacteristic(characteristic.getUuid(), value)
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        oneTimeErrorCallback.execute(BleErrorUtils.cancelled());
                        pendingTransactions.removeSubscription(transactionId);
                    }
                })
                .subscribe(new Observer<byte[]>() {
                    @Override
                    public void onCompleted() {
                        pendingTransactions.removeSubscription(transactionId);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e instanceof BleCharacteristicNotFoundException) {
                            oneTimeErrorCallback.execute(
                                    BleErrorUtils.characteristicNotFound(
                                            UUIDConverter.fromUUID(
                                                    characteristic.getUuid())));
                            return;
                        }
                        oneTimeErrorCallback.execute(errorConverter.toError(e));
                        pendingTransactions.removeSubscription(transactionId);
                    }

                    @Override
                    public void onNext(byte[] bytes) {
                        characteristic.logValue("Write to", bytes);
                        characteristic.setValue(bytes);
                        onSuccessCallback.onSuccess(characteristic);
                    }
                });

        pendingTransactions.replaceSubscription(transactionId, subscription);
    }

    private void safeMonitorCharacteristicForDevice(final Characteristic characteristic,
                                                    final String transactionId,
                                                    final OnEventCallback<Characteristic> onEventCallback,
                                                    final OnErrorCallback onErrorCallback) {
        final RxBleConnection connection = getConnectionOrEmitError(characteristic.getDeviceID(), onErrorCallback);
        if (connection == null) {
            return;
        }

        final OneTimeActionExecutor<BleError> oneTimeErrorCallback = new OneTimeActionExecutor<BleError>() {
            @Override
            public void action(BleError error) {
                onErrorCallback.onError(error);
            }
        };

        final Subscription subscription = Observable.defer(new Func0<Observable<Observable<byte[]>>>() {
            @Override
            public Observable<Observable<byte[]>> call() {
                BluetoothGattDescriptor cccDescriptor = characteristic.getGattDescriptor(Constants.CLIENT_CHARACTERISTIC_CONFIG_UUID);
                NotificationSetupMode setupMode = cccDescriptor != null
                        ? NotificationSetupMode.QUICK_SETUP
                        : NotificationSetupMode.COMPAT;
                if (characteristic.isNotifiable()) {
                    return connection.setupNotification(characteristic.getUuid(), setupMode);
                }

                if (characteristic.isIndicatable()) {
                    return connection.setupIndication(characteristic.getUuid(), setupMode);
                }

                return Observable.error(new CannotMonitorCharacteristicException(characteristic));
            }
        })
                .flatMap(new Func1<Observable<byte[]>, Observable<byte[]>>() {
                    @Override
                    public Observable<byte[]> call(Observable<byte[]> observable) {
                        return observable;
                    }
                })
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        oneTimeErrorCallback.execute(BleErrorUtils.cancelled());
                        pendingTransactions.removeSubscription(transactionId);
                    }
                })
                .subscribe(new Observer<byte[]>() {
                    @Override
                    public void onCompleted() {
                        pendingTransactions.removeSubscription(transactionId);
                    }

                    @Override
                    public void onError(Throwable error) {
                        oneTimeErrorCallback.execute(errorConverter.toError(error));
                        pendingTransactions.removeSubscription(transactionId);
                    }

                    @Override
                    public void onNext(byte[] bytes) {
                        characteristic.logValue("Notification from", bytes);
                        characteristic.setValue(bytes);
                        onEventCallback.onEvent(characteristic);
                    }
                });

        pendingTransactions.replaceSubscription(transactionId, subscription);
    }

    @Nullable
    private Characteristic getCharacteristicOrEmitError(@NonNull final String deviceId,
                                                        @NonNull final String serviceUUID,
                                                        @NonNull final String characteristicUUID,
                                                        @NonNull final OnErrorCallback onErrorCallback) {

        final UUID[] UUIDs = UUIDConverter.convert(serviceUUID, characteristicUUID);
        if (UUIDs == null) {
            onErrorCallback.onError(BleErrorUtils.invalidIdentifiers(serviceUUID, characteristicUUID));
            return null;
        }

        final Device device = connectedDevices.get(deviceId);
        if (device == null) {
            onErrorCallback.onError(BleErrorUtils.deviceNotConnected(deviceId));
            return null;
        }

        final Service service = device.getServiceByUUID(UUIDs[0]);
        if (service == null) {
            onErrorCallback.onError(BleErrorUtils.serviceNotFound(serviceUUID));
            return null;
        }

        final Characteristic characteristic = service.getCharacteristicByUUID(UUIDs[1]);
        if (characteristic == null) {
            onErrorCallback.onError(BleErrorUtils.characteristicNotFound(characteristicUUID));
            return null;
        }

        return characteristic;
    }

    @Nullable
    private Characteristic getCharacteristicOrEmitError(final int serviceIdentifier,
                                                        @NonNull final String characteristicUUID,
                                                        @NonNull final OnErrorCallback onErrorCallback) {

        final UUID uuid = UUIDConverter.convert(characteristicUUID);
        if (uuid == null) {
            onErrorCallback.onError(BleErrorUtils.invalidIdentifiers(characteristicUUID));
            return null;
        }

        final Service service = discoveredServices.get(serviceIdentifier);
        if (service == null) {
            onErrorCallback.onError(BleErrorUtils.serviceNotFound(Integer.toString(serviceIdentifier)));
            return null;
        }

        final Characteristic characteristic = service.getCharacteristicByUUID(uuid);
        if (characteristic == null) {
            onErrorCallback.onError(BleErrorUtils.characteristicNotFound(characteristicUUID));
            return null;
        }

        return characteristic;
    }

    @Nullable
    private Characteristic getCharacteristicOrEmitError(final int characteristicIdentifier,
                                                        @NonNull final OnErrorCallback onErrorCallback) {

        final Characteristic characteristic = discoveredCharacteristics.get(characteristicIdentifier);
        if (characteristic == null) {
            onErrorCallback.onError(BleErrorUtils.characteristicNotFound(Integer.toString(characteristicIdentifier)));
            return null;
        }

        return characteristic;
    }

    private void cleanServicesAndCharacteristicsForDevice(@NonNull Device device) {
        for (int i = discoveredServices.size() - 1; i >= 0; i--) {
            int key = discoveredServices.keyAt(i);
            Service service = discoveredServices.get(key);

            if (service.getDeviceID().equals(device.getId())) {
                discoveredServices.remove(key);
            }
        }
        for (int i = discoveredCharacteristics.size() - 1; i >= 0; i--) {
            int key = discoveredCharacteristics.keyAt(i);
            Characteristic characteristic = discoveredCharacteristics.get(key);

            if (characteristic.getDeviceID().equals(device.getId())) {
                discoveredCharacteristics.remove(key);
            }
        }
    }
}
