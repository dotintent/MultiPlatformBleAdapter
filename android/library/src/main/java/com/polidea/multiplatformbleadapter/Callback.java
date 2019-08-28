package com.polidea.multiplatformbleadapter;

interface Callback<T> {

    void onSuccess(T data);

    void onError(Throwable error);
}
