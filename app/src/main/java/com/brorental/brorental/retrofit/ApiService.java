package com.brorental.brorental.retrofit;

import com.google.gson.JsonObject;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface ApiService {
    @POST
    Call<JsonObject> getCountryState(@Url String url, @Body RequestBody params);
}
