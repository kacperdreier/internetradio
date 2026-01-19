package com.example.internetradio.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.internetradio.MainActivity;
import com.example.internetradio.R;
import com.example.internetradio.data.RadioStation;
import com.example.internetradio.viewmodel.StationViewModel;
import com.google.android.material.button.MaterialButton;

public class StationDetailsFragment extends Fragment {

    private StationViewModel viewModel;
    private RadioStation currentStation;
    private MainActivity mainActivity;
    private String stationUuid;
    private Button favoriteButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_station_details, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(StationViewModel.class);
        mainActivity = (MainActivity) getActivity();

        TextView name = view.findViewById(R.id.text_detail_name);
        TextView country = view.findViewById(R.id.text_detail_country);
        TextView urlText = view.findViewById(R.id.text_detail_url);
        favoriteButton = view.findViewById(R.id.favorite_button);
        Button deleteButton = view.findViewById(R.id.button_delete);
        Button playButton = view.findViewById(R.id.button_play);

        if (getArguments() != null) {
            stationUuid = getArguments().getString("stationUuid");
            viewModel.getStationByUuid(stationUuid).observe(getViewLifecycleOwner(), station -> {
                if (station != null) {
                    currentStation = station;
                    name.setText(station.getName());
                    country.setText(station.getCountry());
                    urlText.setText(station.getUrl());
                    updateFavoriteButtonState(station.isFavorite());
                }
            });
        }

        deleteButton.setOnClickListener(v -> {
            if (currentStation != null) {
                viewModel.delete(currentStation);
                Navigation.findNavController(view).popBackStack();
            }
        });

        favoriteButton.setOnClickListener(v -> {
            if (currentStation != null) {
                boolean currentlyFavorite = currentStation.isFavorite();
                boolean newState = !currentlyFavorite;

                if (mainActivity != null && mainActivity.getBleManager() != null) {
                    if (newState) {
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
                            Toast.makeText(getContext(), "Błąd: Stacja nie jest w radiu", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    updateFavoriteButtonState(newState);

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        mainActivity.getBleManager().sendCommand("LIST");
                    }, 1500);

                } else {
                    Toast.makeText(getContext(), "Brak połączenia z radiem!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        playButton.setOnClickListener(v -> {
            if (currentStation != null) {
                int index = currentStation.getEspIndex();
                if (mainActivity != null && mainActivity.getBleManager() != null) {
                    if (index != -1) {
                        mainActivity.getBleManager().sendCommand("PLAY " + index);
                    } else {
                        Toast.makeText(getContext(), "Dodaj stację do ulubionych, aby zagrać!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Brak połączenia z radiem!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private void updateFavoriteButtonState(boolean isFavorite) {
        if (isFavorite) {
            favoriteButton.setText("USUŃ Z ULUBIONYCH");
            favoriteButton.setTextColor(Color.parseColor("#F57C00"));
        } else {
            favoriteButton.setText("DODAJ DO ULUBIONYCH");
            favoriteButton.setTextColor(Color.GRAY);
        }
    }
}