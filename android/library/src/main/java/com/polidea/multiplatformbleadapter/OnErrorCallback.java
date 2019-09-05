package com.polidea.multiplatformbleadapter;

import com.polidea.multiplatformbleadapter.errors.BleError;

public interface OnErrorCallback {

    void onError(BleError error);
}
