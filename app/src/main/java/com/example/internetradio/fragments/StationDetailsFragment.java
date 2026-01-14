package com.example.internetradio.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.internetradio.MainActivity;
import com.example.internetradio.R;
import com.example.internetradio.bluetooth.BleManager;
import com.example.internetradio.data.RadioStation;
import com.example.internetradio.viewmodel.StationViewModel;

public class StationDetailsFragment extends Fragment {

    private StationViewModel viewModel;
    private RadioStation currentStation;
    private com.example.internetradio.MainActivity mainActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_station_details, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(StationViewModel.class);
        mainActivity = (com.example.internetradio.MainActivity) getActivity();

        TextView name = view.findViewById(R.id.text_detail_name);
        TextView country = view.findViewById(R.id.text_detail_country);
        ToggleButton favoriteToggle = view.findViewById(R.id.toggle_favorite);
        Button deleteButton = view.findViewById(R.id.button_delete);
        Button playButton = view.findViewById(R.id.button_play);

        if (getArguments() != null) {
            String stationUuid = getArguments().getString("stationUuid");

            viewModel.getStationByUuid(stationUuid).observe(getViewLifecycleOwner(), station -> {
                if (station != null) {
                    currentStation = station;
                    name.setText(station.getName());
                    country.setText(station.getCountry());
                    favoriteToggle.setChecked(station.isFavorite());
                }
            });
        }

        favoriteToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentStation != null) {
                currentStation.setFavorite(isChecked);
                viewModel.update(currentStation);
            }
        });

        deleteButton.setOnClickListener(v -> {
            if (currentStation != null) {
                viewModel.delete(currentStation);
                Navigation.findNavController(view).popBackStack();
            }
        });

//        playButton.setOnClickListener(v -> {
//            if (currentStation != null && getActivity() instanceof MainActivity) {
//                BleManager ble = ((MainActivity) getActivity()).getBleManager();
//
//                // Sprawdź czy stacja ma URL
//                String url = currentStation.getUrl();
//                if (url == null || url.isEmpty()) return;
//
//                // Zamień spacje na podkreślniki (ważne dla parsera w ESP32) [cite: 2026-01-07]
//                String cleanName = currentStation.getName().replace(" ", "_");
//
//                // Wysyłaj komendy z większym odstępem, aby ESP32 zdążyło je przetworzyć
//                ble.sendCommand("ADD " + url + " " + cleanName); // [cite: 2026-01-07]
//
//                playButton.postDelayed(() -> {
//                    // Spróbujmy PLAY 1 (zakładając że to druga stacja po domyślnej) [cite: 2026-01-07]
//                    ble.sendCommand("PLAY 1");
//                }, 1500); // 1.5 sekundy przerwy
//            }
//        });

        return view;
    }
}