package com.polidea.multiplatformbleadapter.event;

public class CharacteristicValueChangeEvent extends Event {

    private int characteristicId;
    private byte[] bytes;

    public CharacteristicValueChangeEvent(int characteristicId, byte[] bytes) {
        this.characteristicId = characteristicId;
        this.bytes = bytes;
    }

    public int getCharacteristicId() {
        return characteristicId;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
