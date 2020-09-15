package com.polidea.multiplatformbleadapter;

import androidx.annotation.Nullable;

import com.polidea.multiplatformbleadapter.utils.Constants.ConnectionPriority;

public class ConnectionOptions {

    /**
     * Whether to directly connect to the remote device (false) or to automatically connect as soon
     * as the remote device becomes available (true).
     */
    private boolean autoConnect;

    /**
     * Whether MTU size will be negotiated to this value. It is not guaranteed to get it after
     * connection is successful.
     */
    private int requestMTU;

    /**
     * Whether action will be taken to reset services cache. This option may be useful when a
     * peripheral's firmware was updated and it's services/characteristics were
     * added/removed/altered. {@link https://stackoverflow.com/questions/22596951/how-to-programmatically-force-bluetooth-low-energy-service-discovery-on-android}
     */
    private RefreshGattMoment refreshGattMoment;

    /**
     * Number of milliseconds after connection is automatically timed out. In case of race condition
     * were connection is established right after timeout event, device will be disconnected
     * immediately. Time out may happen earlier then specified due to OS specific behavior.
     */
    @Nullable
    private Long timeoutInMillis;

    @ConnectionPriority
    private int connectionPriority;

    public ConnectionOptions(Boolean autoConnect,
                             int requestMTU,
                             RefreshGattMoment refreshGattMoment,
                             @Nullable Long timeoutInMillis,
                             int connectionPriority) {
        this.autoConnect = autoConnect;
        this.requestMTU = requestMTU;
        this.refreshGattMoment = refreshGattMoment;
        this.timeoutInMillis = timeoutInMillis;
        this.connectionPriority = connectionPriority;
    }

    public Boolean getAutoConnect() {
        return autoConnect;
    }

    public int getRequestMTU() {
        return requestMTU;
    }

    public RefreshGattMoment getRefreshGattMoment() {
        return refreshGattMoment;
    }

    @Nullable
    public Long getTimeoutInMillis() {
        return timeoutInMillis;
    }

    @ConnectionPriority
    public int getConnectionPriority() {
        return connectionPriority;
    }
}
