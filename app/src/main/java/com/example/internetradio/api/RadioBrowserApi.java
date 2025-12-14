package com.example.internetradio.api; // Utwórz nowy pakiet 'api'

import com.example.internetradio.data.RadioStation;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface RadioBrowserApi {
    @GET("stations/bycountry/poland")
    Call<List<RadioStation>> getStationsByCountry();
}