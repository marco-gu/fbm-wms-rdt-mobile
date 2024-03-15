package com.maersk.fbm.rdt_mobile.adapter;

import androidx.lifecycle.LiveData;

import com.google.gson.Gson;
import com.maersk.fbm.rdt_mobile.entity.ApiResponseEntity;


import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyLiveData<T> extends LiveData<T> {

    private final AtomicBoolean started = new AtomicBoolean(false);
    private final Call<T> call;

    public MyLiveData(Call<T> call) {
        this.call = call;
    }


    @Override
    protected void onActive() {
        super.onActive();
        if (started.compareAndSet(false, true)) {
            call.enqueue(new Callback<T>() {
                @Override
                public void onResponse( Call<T> call, Response<T> response) {
                    Gson gson = new Gson();
                    String url = response.raw().request().url().encodedPath();
                    ApiResponseEntity res = new ApiResponseEntity();
                    com.maersk.fbm.rdt_mobile.adapter.MyLiveData.super.postValue((T) res);
                }

                @Override
                public void onFailure( Call<T> call,  Throwable t) {
                    String url = call.request().url().encodedPath();
                    ApiResponseEntity res = new ApiResponseEntity();
                    com.maersk.fbm.rdt_mobile.adapter.MyLiveData.super.postValue((T) res);
                }
            });
        }
    }
}
