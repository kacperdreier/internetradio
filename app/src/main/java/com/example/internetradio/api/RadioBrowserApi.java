package com.example.internetradio.api;

import com.example.internetradio.data.RadioStation;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface RadioBrowserApi {
    @GET("stations/search")
    Call<List<RadioStation>> getSafeStations(
            @retrofit2.http.Query("country") String country,
            @retrofit2.http.Query("bitrateMax") int bitrateMax,
            @retrofit2.http.Query("hidebroken") boolean hideBroken,
            @retrofit2.http.Query("order") String order
    );
}