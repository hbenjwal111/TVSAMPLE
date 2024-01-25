package com.oxootv.spagreen.network.api;

import com.oxootv.spagreen.model.HomeContent;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface HomeApi {

    @GET("home_content")
    Call<List<HomeContent>> getHomeContent(@Header("API-KEY") String apiKey);
}
