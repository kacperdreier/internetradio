package com.example.internetradio.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.internetradio.MainActivity;
import com.example.internetradio.R;
import com.example.internetradio.data.RadioStation;
import com.example.internetradio.viewmodel.StationViewModel;

public class StationDetailsFragment extends Fragment {

    private StationViewModel viewModel;
    private RadioStation currentStation;
    private MainActivity mainActivity;
    private String stationUuid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_station_details, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(StationViewModel.class);
        mainActivity = (MainActivity) getActivity();

        TextView name = view.findViewById(R.id.text_detail_name);
        TextView country = view.findViewById(R.id.text_detail_country);
        ToggleButton favoriteToggle = view.findViewById(R.id.toggle_favorite);
        Button deleteButton = view.findViewById(R.id.button_delete);
        Button playButton = view.findViewById(R.id.button_play);

        if (getArguments() != null) {
            stationUuid = getArguments().getString("stationUuid");
            viewModel.getStationByUuid(stationUuid).observe(getViewLifecycleOwner(), station -> {
                if (station != null) {
                    currentStation = station; // To aktualizuje obiekt w całym fragmencie
                    name.setText(station.getName());
                    country.setText(station.getCountry());
                    favoriteToggle.setChecked(station.isFavorite());
                    Log.d("RADIO_DEBUG", "Obserwator odświeżył stację. Obecny index: " + station.getEspIndex());
                }
            });
        }

        deleteButton.setOnClickListener(v -> {
            if (currentStation != null) {
                viewModel.delete(currentStation);
                Navigation.findNavController(view).popBackStack();
            }
        });

        favoriteToggle.setOnClickListener(v -> {
            if (currentStation != null) {
                boolean isChecked = favoriteToggle.isChecked();

                if (mainActivity != null && mainActivity.getBleManager() != null) {
                    if (isChecked) {
                        String cleanName = currentStation.getName().replace(" ", "_");
                        String cmd = "ADD " + currentStation.getUrl() + " " + cleanName;
                        mainActivity.getBleManager().sendCommand(cmd);
                        Toast.makeText(getContext(), "Wysłano żądanie dodania...", Toast.LENGTH_SHORT).show();
                    } else {
                        int idx = currentStation.getEspIndex();
                        if (idx != -1) {
                            mainActivity.getBleManager().sendCommand("DELETE " + idx);
                            Toast.makeText(getContext(), "Wysłano żądanie usunięcia...", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Błąd: Stacja nie ma indeksu w radiu", Toast.LENGTH_SHORT).show();
                        }
                    }

                    new Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        mainActivity.getBleManager().sendCommand("LIST");
                    }, 1500);
                } else {
                    favoriteToggle.setChecked(!isChecked);
                    Toast.makeText(getContext(), "Brak połączenia z radiem!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        playButton.setOnClickListener(v -> {
            if (currentStation != null) {
                int index = currentStation.getEspIndex();

                Log.d("RADIO_DEBUG", "Kliknięto PLAY. Index: " + index);

                if (mainActivity != null && mainActivity.getBleManager() != null) {
                    if (index != -1) {
                        mainActivity.getBleManager().sendCommand("PLAY " + index);
                        Toast.makeText(getContext(), "Wysyłam PLAY " + index, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Ta stacja nie jest w pamięci radia!", Toast.LENGTH_SHORT).show();
                        Log.e("RADIO_DEBUG", "Błąd: Próba PLAY na indeksie -1");
                    }
                }
            } else {
                Log.e("RADIO_DEBUG", "Błąd: currentStation jest NULL");
            }
        });

        return view;
    }
}