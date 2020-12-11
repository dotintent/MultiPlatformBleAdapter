package com.polidea.multiplatformbleadapter.utils;

import android.bluetooth.BluetoothGatt;
import androidx.annotation.NonNull;

import com.polidea.rxandroidble2.RxBleCustomOperation;
import com.polidea.rxandroidble2.internal.RxBleLog;
import com.polidea.rxandroidble2.internal.connection.RxBleGattCallback;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Scheduler;


public class RefreshGattCustomOperation implements RxBleCustomOperation<Boolean> {

    @NonNull
    @Override
    public Observable<Boolean> asObservable(
            final BluetoothGatt bluetoothGatt,
            final RxBleGattCallback rxBleGattCallback,
            final Scheduler scheduler
    ) throws Throwable {

        return Observable.ambArray(
                Observable.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        boolean success = false;
                        call: try {
                            Method bluetoothGattRefreshFunction = bluetoothGatt.getClass().getMethod("refresh");
                            if (bluetoothGattRefreshFunction == null) {
                                RxBleLog.d("Could not find function BluetoothGatt.refresh()");
                                break call;
                            }

                            success = (Boolean) bluetoothGattRefreshFunction.invoke(bluetoothGatt);

                            if (!success) RxBleLog.d("BluetoothGatt.refresh() returned false");
                        } catch (Exception e) {
                            RxBleLog.d(e, "Could not call function BluetoothGatt.refresh()");
                        }

                        RxBleLog.i("Calling BluetoothGatt.refresh() status: %s", success ? "Success" : "Failure");
                        return success;
                    }
                })
                        .subscribeOn(scheduler)
                        .delay(1, TimeUnit.SECONDS, scheduler),
                rxBleGattCallback.<Boolean>observeDisconnect()
        );
    }
}
