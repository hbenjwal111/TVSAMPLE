package com.sampletv.spagreen.network.api;


import com.sampletv.spagreen.model.SearchModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface SearchApi {

    @GET("search")
    Call<SearchModel> getSearchData(@Header("API-KEY") String key,
                                    @Query("q") String query,
                                    @Query("page") int page,
                                    @Query("type") String type);
}
