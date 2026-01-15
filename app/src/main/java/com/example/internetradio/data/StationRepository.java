package com.example.internetradio.data;

import android.app.Application;
import android.util.Log;
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
        apiService.getSafeStations("Poland", 128, true, "bitrate").enqueue(new Callback<List<RadioStation>>() {
            @Override
            public void onResponse(Call<List<RadioStation>> call, Response<List<RadioStation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<RadioStation> allFetched = response.body();

                    java.util.List<RadioStation> safeStations = new java.util.ArrayList<>();

                    for (RadioStation s : allFetched) {
                        if (s.getBitrate() > 0 && s.getBitrate() <= 128) {
                            String cleanName = s.getName().replaceAll("[^a-zA-Z0-9]", "_");
                            s.setName(cleanName);
                            safeStations.add(s);
                        }
                    }

                    databaseWriteExecutor.execute(() -> {
                        stationDao.deleteAll();
                        stationDao.insertAll(safeStations);
                        Log.d("Repository", "Zapisano " + safeStations.size() + " bezpiecznych stacji (max 128kbps)");
                    });

                }
            }

            @Override
            public void onFailure(Call<List<RadioStation>> call, Throwable t) {
                Log.e("Repository", "Błąd sieci: " + t.getMessage());
            }
        });
    }
    public LiveData<RadioStation> getStationByUuid(String uuid) {
        return stationDao.getStationByUuid(uuid);
    }

    public void delete(RadioStation station) {
        databaseWriteExecutor.execute(() -> {
            stationDao.delete(station);
        });
    }

    public void update(RadioStation station) {
        databaseWriteExecutor.execute(() -> {
            stationDao.update(station);
        });
    }
    public void insert(RadioStation station) {
        databaseWriteExecutor.execute(() -> {
            stationDao.insert(station);
        });
    }
    public void checkAndFetchStations() {
        databaseWriteExecutor.execute(() -> {
            int count = stationDao.getStationCount();

            if (count == 0) {
                Log.d("Repository", "Baza jest pusta. Rozpoczynam pobieranie danych z API.");
                fetchStationsAndSave();
            } else {
                Log.d("Repository", "Baza zawiera " + count + " stacji. Pomijam pobieranie API.");
            }
        });
    }
    public LiveData<List<RadioStation>> getFavoriteStations() {
        return stationDao.getFavoriteStations();
    }
    public void updateFavoriteStatus(RadioStation station) {
        databaseWriteExecutor.execute(() -> {
            stationDao.update(station);
        });
    }
    public void resetAllIndices() {
        new Thread(() -> {
            stationDao.resetAllEspIndices();
        }).start();
    }
    public List<RadioStation> getFavoriteStationsSync() {
        final List<RadioStation>[] result = new List[1];
        Thread thread = new Thread(() -> {
            result[0] = stationDao.getFavoriteStationsSync();
        });

        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result[0] != null ? result[0] : new java.util.ArrayList<>();
    }
}