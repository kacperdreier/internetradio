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
    }

    public LiveData<List<RadioStation>> getAllStations() { return allStations; }
    public LiveData<List<RadioStation>> getFavoriteStations() { return repository.getFavoriteStations(); }
    public LiveData<RadioStation> getStationByUuid(String uuid) { return repository.getStationByUuid(uuid); }

    public void insert(RadioStation station) { repository.insert(station); }
    public void delete(RadioStation station) { repository.delete(station); }
    public void resetAllIndices() { repository.resetAllIndices(); }

    public void update(RadioStation station) {
        repository.updateFavoriteStatus(station);
    }

    public void updateFavoriteStatus(RadioStation station) {
        repository.updateFavoriteStatus(station);
    }


    public List<RadioStation> getAllStationsSync() {
        return repository.getAllStationsSync();
    }

    public List<RadioStation> getFavoriteStationsSync() {
        return repository.getFavoriteStationsSync();
    }

    public void clearAllFavorites() {
        repository.clearAllFavorites();
    }

    public int getNextEspIndex() {
        return 1;
    }
}