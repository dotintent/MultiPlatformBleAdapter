package com.polidea.multiplatformbleadapter;


enum RefreshGattMoment {

    ON_CONNECTED("OnConnected");

    final String name;

    RefreshGattMoment(String name) {
        this.name = name;
    }
}
