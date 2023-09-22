package com.polidea.multiplatformbleadapter.utils.mapper;

import com.polidea.multiplatformbleadapter.AdvertisementData;
import com.polidea.multiplatformbleadapter.ScanResult;
import com.polidea.multiplatformbleadapter.utils.Constants;

public class RxScanResultToScanResultMapper {

    public ScanResult map(com.polidea.rxandroidble2.scan.ScanResult rxScanResult) {
        return new ScanResult(
                rxScanResult.getBleDevice().getMacAddress(),
                rxScanResult.getBleDevice().getName(),
                rxScanResult.getRssi(),
                Constants.MINIMUM_MTU,
                false, //isConnectable flag is not available on Android
                null, //overflowServiceUUIDs are not available on Android
                AdvertisementData.parseScanResponseData(rxScanResult.getScanRecord().getBytes())
        );
    }
}
