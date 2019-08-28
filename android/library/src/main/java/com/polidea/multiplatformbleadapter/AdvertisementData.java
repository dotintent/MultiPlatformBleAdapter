package com.polidea.multiplatformbleadapter;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class AdvertisementData {

    private byte[] manufacturerData;
    private Map<UUID, byte[]> serviceData;
    private ArrayList<UUID> serviceUUIDs;
    private String localName;
    private Integer txPowerLevel;
    private ArrayList<UUID> solicitedServiceUUIDs;

    public AdvertisementData(byte[] manufacturerData,
                             Map<UUID, byte[]> serviceData,
                             ArrayList<UUID> serviceUUIDs,
                             String localName,
                             Integer txPowerLevel,
                             ArrayList<UUID> solicitedServiceUUIDs) {
        this.manufacturerData = manufacturerData;
        this.serviceData = serviceData;
        this.serviceUUIDs = serviceUUIDs;
        this.localName = localName;
        this.txPowerLevel = txPowerLevel;
        this.solicitedServiceUUIDs = solicitedServiceUUIDs;
    }

    public byte[] getManufacturerData() {
        return manufacturerData;
    }

    public Map<UUID, byte[]> getServiceData() {
        return serviceData;
    }

    public ArrayList<UUID> getServiceUUIDs() {
        return serviceUUIDs;
    }

    public String getLocalName() {
        return localName;
    }

    public Integer getTxPowerLevel() {
        return txPowerLevel;
    }

    public ArrayList<UUID> getSolicitedServiceUUIDs() {
        return solicitedServiceUUIDs;
    }
}
