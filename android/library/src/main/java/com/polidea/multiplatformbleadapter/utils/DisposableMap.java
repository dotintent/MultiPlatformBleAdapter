package com.polidea.multiplatformbleadapter.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.reactivex.disposables.Disposable;


public class DisposableMap {

    final private Map<String, Disposable> Disposables = new HashMap<>();

    public synchronized void replaceDisposable(String key, Disposable Disposable) {
        Disposable oldDisposable = Disposables.put(key, Disposable);
        if (oldDisposable != null && !oldDisposable.isDisposed()) {
            oldDisposable.dispose();
        }
    }

    public synchronized boolean removeDisposable(String key) {
        Disposable Disposable = Disposables.remove(key);
        if (Disposable == null) return false;
        if (!Disposable.isDisposed()) {
            Disposable.dispose();
        }
        return true;
    }

    public synchronized void removeAllDisposables() {
        Iterator<Map.Entry<String, Disposable>> it = Disposables.entrySet().iterator();
        while (it.hasNext()) {
            Disposable Disposable = it.next().getValue();
            it.remove();
            if (!Disposable.isDisposed()) {
                Disposable.dispose();
            }
        }
    }
}
