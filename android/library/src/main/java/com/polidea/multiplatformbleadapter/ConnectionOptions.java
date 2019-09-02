package com.polidea.multiplatformbleadapter;

class ConnectionOptions {

    /**
     * Whether to directly connect to the remote device (false) or to automatically connect as soon
     * as the remote device becomes available (true).
     */
    private Boolean autoConnect;

    /**
     * Whether MTU size will be negotiated to this value. It is not guaranteed to get it after
     * connection is successful.
     */
    private String requestMTU;

    /**
     * Whether action will be taken to reset services cache. This option may be useful when a
     * peripheral's firmware was updated and it's services/characteristics were
     * added/removed/altered. {@link https://stackoverflow.com/questions/22596951/how-to-programmatically-force-bluetooth-low-energy-service-discovery-on-android}
     */
    private String refreshGatt;

    /**
     * Number of milliseconds after connection is automatically timed out. In case of race condition
     * were connection is established right after timeout event, device will be disconnected
     * immediately. Time out may happen earlier then specified due to OS specific behavior.
     */
    private Long timeoutInMillis;

    public ConnectionOptions(Boolean autoConnect, String requestMTU, String refreshGatt, Long timeoutInMillis) {
        this.autoConnect = autoConnect;
        this.requestMTU = requestMTU;
        this.refreshGatt = refreshGatt;
        this.timeoutInMillis = timeoutInMillis;
    }

    public Boolean getAutoConnect() {
        return autoConnect;
    }

    public String getRequestMTU() {
        return requestMTU;
    }

    public String getRefreshGatt() {
        return refreshGatt;
    }

    public Long getTimeoutInMillis() {
        return timeoutInMillis;
    }
}