package com.example.internetradio.viewmodel; // Utwórz nowy pakiet 'viewmodel'

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

        repository.fetchStationsAndSave();
    }

    public LiveData<List<RadioStation>> getAllStations() {
        return allStations;
    }

    public void refreshStations() {
        repository.fetchStationsAndSave();
    }
}