package com.example.modasense;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("recommend")
    Call<RecommendationResponse> getRecommendation(
            @Query("season") String season,
            @Query("usage") String usage,
            @Query("gender") String gender
    );
}
