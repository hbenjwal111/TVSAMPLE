package com.sampletv.spagreen.network.api;



import com.sampletv.spagreen.model.Movie;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface TvSeriesApi {

    @GET("tvseries")
    Call<List<Movie>> getTvSeries(@Header("API-KEY") String apiKey,
                                  @Query("page") int page);

}
