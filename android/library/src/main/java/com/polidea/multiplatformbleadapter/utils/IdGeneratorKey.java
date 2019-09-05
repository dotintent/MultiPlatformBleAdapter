package com.polidea.multiplatformbleadapter.utils;


import java.util.UUID;

public class IdGeneratorKey {

    private final String deviceAddress;
    private final UUID uuid;
    private final int id;

    public IdGeneratorKey(String deviceAddress, UUID uuid, int id) {
        this.deviceAddress = deviceAddress;
        this.uuid = uuid;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IdGeneratorKey)) {
            return false;
        }

        IdGeneratorKey that = (IdGeneratorKey) o;

        return id == that.id &&
                deviceAddress.equals(that.deviceAddress) &&
                uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        int result = deviceAddress.hashCode();
        result = 31 * result + uuid.hashCode();
        result = 31 * result + id;
        return result;
    }
}
