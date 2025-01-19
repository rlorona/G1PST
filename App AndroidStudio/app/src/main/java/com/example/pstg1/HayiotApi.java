package com.example.pstg1;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import java.util.List;

public interface HayiotApi {
    @GET("api/hayiot/getData")
    Call<List<SensorData>> getSensorData(
            @Query("id") String id,
            @Query("start") String startDate,
            @Query("end") String endDate
    );
}