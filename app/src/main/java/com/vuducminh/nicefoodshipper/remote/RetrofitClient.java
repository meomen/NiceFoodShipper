package com.vuducminh.nicefoodshipper.remote;

import com.vuducminh.nicefoodshipper.common.CommonAgr;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;


// Nhận lại response từ Google API
public class RetrofitClient {
    private static Retrofit instance;

    public static Retrofit getInstance() {
        return  instance == null ? new Retrofit.Builder()
                .baseUrl(CommonAgr.URL_MAP_GOOGLE)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build() : instance;
    }
}
