package com.polidea.multiplatformbleadapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;
import java.util.UUID;

public class Device {

    private String id;
    private String name;
    private int rssi;
    private int mtu;
    @Nullable
    private List<Service> services;

    public Device(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public int getMtu() {
        return mtu;
    }

    public void setMtu(int mtu) {
        this.mtu = mtu;
    }

    @Nullable
    public List<Service> getServices() {
        return services;
    }

    public void setServices(@Nullable List<Service> services) {
        this.services = services;
    }

    @Nullable
    public Service getServiceByUUID(@NonNull UUID uuid) {
        if (services == null) {
            return null;
        }

        for (Service service : services) {
            if (uuid.equals(service.getUuid()))
                return service;
        }
        return null;
    }
}
