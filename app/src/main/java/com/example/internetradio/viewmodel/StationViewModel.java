package com.example.internetradio.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.internetradio.data.RadioStation;
import com.example.internetradio.data.StationRepository;
import java.util.List;

public class StationViewModel extends AndroidViewModel {

    private StationRepository repository;
    private final LiveData<List<RadioStation>> allStations;

    public StationViewModel(@NonNull Application application) {
        super(application);
        repository = new StationRepository(application);
        allStations = repository.getAllStations();

        repository.checkAndFetchStations();
    }

    public LiveData<List<RadioStation>> getAllStations() {
        return allStations;
    }

    public void refreshStations() {
        repository.fetchStationsAndSave();
    }
    public LiveData<RadioStation> getStationByUuid(String uuid) {
        return repository.getStationByUuid(uuid);
    }

    public void delete(RadioStation station) {
        repository.delete(station);
    }

    public void update(RadioStation station) {
        repository.update(station);
    }
    public void insert(RadioStation station) {
        repository.insert(station);
    }
    public LiveData<List<RadioStation>> getFavoriteStations() {
        return repository.getFavoriteStations();
    }

    public void updateFavoriteStatus(RadioStation station) {
        repository.updateFavoriteStatus(station);
    }

    public int getNextEspIndex() {
        List<RadioStation> favorites = repository.getFavoriteStationsSync();

        if (favorites == null || favorites.isEmpty()) {
            return 1;
        }

        int maxIdx = 0;
        for (RadioStation s : favorites) {
            if (s.getEspIndex() > maxIdx) {
                maxIdx = s.getEspIndex();
            }
        }

        if (maxIdx >= 10) {
            return -1;
        }

        return maxIdx + 1;
    }

    public void resetAllIndices() {
        repository.resetAllIndices();
    }

}