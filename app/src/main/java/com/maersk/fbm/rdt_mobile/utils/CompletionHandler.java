package com.maersk.fbm.rdt_mobile.utils;

public interface  CompletionHandler<T> {
    void complete(T retValue);
    void complete();
    void setProgressData(T value);
}
