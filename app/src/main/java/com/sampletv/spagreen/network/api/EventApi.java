package com.sampletv.spagreen.network.api;


import com.sampletv.spagreen.model.Event;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface EventApi {

    @GET("get_event")
    Call<List<Event>> getAllEvent(@Query("api_secret_key") String apiKey);
}
