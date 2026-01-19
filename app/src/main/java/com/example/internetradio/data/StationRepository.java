package com.example.internetradio.data;

import android.app.Application;
import androidx.lifecycle.LiveData; // Ten import naprawia błędy z LiveData
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StationRepository {
    private StationDao stationDao;
    // Tworzymy własny executor, żeby nie polegać na StationDatabase
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public StationRepository(Application application) {
        StationDatabase db = StationDatabase.getDatabase(application);
        stationDao = db.stationDao();
    }

    public LiveData<List<RadioStation>> getAllStations() { return stationDao.getAllStations(); }
    public LiveData<List<RadioStation>> getFavoriteStations() { return stationDao.getFavoriteStations(); }
    public LiveData<RadioStation> getStationByUuid(String uuid) { return stationDao.getStationByUuid(uuid); }

    public void insert(RadioStation station) {
        executor.execute(() -> stationDao.insert(station));
    }

    public void delete(RadioStation station) {
        executor.execute(() -> stationDao.delete(station));
    }

    public void resetAllIndices() {
        executor.execute(() -> stationDao.resetAllEspIndices());
    }

    // Twoja metoda aktualizacji
    public void updateFavoriteStatus(RadioStation station) {
        executor.execute(() -> stationDao.update(station));
    }

    // --- NOWE METODY DO OBSŁUGI BRAKUJĄCYCH FUNKCJI ---

    public List<RadioStation> getAllStationsSync() {
        return stationDao.getAllStationsSync();
    }

    public List<RadioStation> getFavoriteStationsSync() {
        return stationDao.getFavoriteStationsSync();
    }

    public void clearAllFavorites() {
        executor.execute(() -> stationDao.clearAllFavorites());
    }
}