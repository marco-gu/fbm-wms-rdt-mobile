package com.maersk.fbm.rdt_mobile.adapter;

import androidx.lifecycle.LiveData;



import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.CallAdapter;

public class LiveDataCallAdapter<T> implements CallAdapter<T, LiveData<T>> {

    private final Type mResponseType;

    public LiveDataCallAdapter(Type mResponseType) {
        this.mResponseType = mResponseType;
    }

    @Override
    public Type responseType() {
        return mResponseType;
    }

    @Override
    public LiveData<T> adapt(Call<T> call) {
        return new MyLiveData<>(call);
    }
}
