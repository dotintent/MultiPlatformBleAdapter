package com.polidea.multiplatformbleadapter.event;

import com.polidea.multiplatformbleadapter.ScanResult;

public class ScanEvent extends Event {

    private ScanResult scanResult;

    public ScanEvent(ScanResult scanResult) {
        this.scanResult = scanResult;
    }

    public ScanResult getScanResult() {
        return scanResult;
    }
}