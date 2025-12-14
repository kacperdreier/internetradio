package com.example.internetradio.data;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.internetradio.api.RadioBrowserApi;
import com.example.internetradio.api.RetrofitClient;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StationRepository {

    private StationDao stationDao;
    private RadioBrowserApi apiService;
    private LiveData<List<RadioStation>> allStations;
    private static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(4);

    public StationRepository(Application application) {
        StationDatabase db = StationDatabase.getDatabase(application);
        stationDao = db.stationDao();
        allStations = stationDao.getAllStations();

        apiService = RetrofitClient.getRetrofitInstance().create(RadioBrowserApi.class);
    }

    public LiveData<List<RadioStation>> getAllStations() {
        return allStations;
    }

    public void fetchStationsAndSave() {
        apiService.getStationsByCountry().enqueue(new Callback<List<RadioStation>>() {
            @Override
            public void onResponse(Call<List<RadioStation>> call, Response<List<RadioStation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<RadioStation> stations = response.body();

                    databaseWriteExecutor.execute(() -> {
                        stationDao.insertAll(stations);
                    });

                } else {
                    System.out.println("Błąd API: Nieudane pobieranie stacji.");
                }
            }

            @Override
            public void onFailure(Call<List<RadioStation>> call, Throwable t) {
                System.out.println("Błąd sieci: " + t.getMessage());
            }
        });
    }
}