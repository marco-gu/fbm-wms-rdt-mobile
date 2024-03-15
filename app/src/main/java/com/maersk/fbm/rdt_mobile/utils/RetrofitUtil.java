package com.maersk.fbm.rdt_mobile.utils;



import com.maersk.fbm.rdt_mobile.adapter.LiveDataCallAdapterFactory;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitUtil {

    private static RetrofitUtil retrofitUtils;
    private RetrofitUtil() {
    }
    public static RetrofitUtil getInstance() {

        if (retrofitUtils == null) {
            synchronized (RetrofitUtil.class) {
                if (retrofitUtils == null) {
                    retrofitUtils = new RetrofitUtil();
                }
            }
        }
        return retrofitUtils;
    }

    public Retrofit getRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .client(getClient().build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(new LiveDataCallAdapterFactory())
                .build();
        return retrofit;
    }

    private OkHttpClient.Builder getClient() {
        // Timeout 30S
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS);
        return httpClientBuilder;
    }

}
