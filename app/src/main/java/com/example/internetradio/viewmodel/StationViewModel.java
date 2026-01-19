package com.example.internetradio.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData; // Ten import jest kluczowy!
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

    // Przekierowanie update() na metodę w repozytorium
    public void update(RadioStation station) {
        repository.updateFavoriteStatus(station);
    }

    public void updateFavoriteStatus(RadioStation station) {
        repository.updateFavoriteStatus(station);
    }

    // --- METODY SYNCHRONICZNE DLA MAIN ACTIVITY ---

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
        // Metoda zostawiona dla kompatybilności, choć teraz rządzi ESP
        return 1;
    }
}