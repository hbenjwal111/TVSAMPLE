package com.sampletv.spagreen.network.api;



import com.sampletv.spagreen.model.User;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface LoginApi {

    @FormUrlEncoded
    @POST("login")
    Call<User> postLoginStatus(@Header("API-KEY") String apiKey,
                               @Field("email") String email,
                               @Field("password") String password);
}
