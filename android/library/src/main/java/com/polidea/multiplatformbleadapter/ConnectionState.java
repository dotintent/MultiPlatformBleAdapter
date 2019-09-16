package com.polidea.multiplatformbleadapter;

public enum ConnectionState {

    CONNECTING("connecting"), CONNECTED("connected"), DISCONNECTING("disconnecting"), DISCONNECTED("disconnected");

    String name;

    ConnectionState(String name) {
        this.name = name;
    }
}
