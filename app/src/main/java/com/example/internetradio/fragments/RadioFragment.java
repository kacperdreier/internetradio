package com.example.internetradio.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.internetradio.MainActivity;
import com.example.internetradio.R;
import com.example.internetradio.bluetooth.BleManager;

public class RadioFragment extends Fragment {

    private ImageButton playPauseButton, prevButton, nextButton;
    private Button volUpButton, volDownButton, vol15Button, connectButton;
    private BleManager bleManager;
    private TextView stationNameTextView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_radio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        stationNameTextView = view.findViewById(R.id.station_name_text);
        playPauseButton = view.findViewById(R.id.button_play_pause);
        prevButton = view.findViewById(R.id.button_prev_station);
        nextButton = view.findViewById(R.id.button_next_station);
        volUpButton = view.findViewById(R.id.button_vol_plus);
        volDownButton = view.findViewById(R.id.button_vol_minus);
        vol15Button = view.findViewById(R.id.button_vol_15);
        connectButton = view.findViewById(R.id.button_ble_connect);

        setupBleManager();

        connectButton.setOnClickListener(v -> {
            if (bleManager != null) bleManager.connectToRadio();
        });

        nextButton.setOnClickListener(v -> sendBleCommand("NEXT"));
        prevButton.setOnClickListener(v -> sendBleCommand("PREV"));
        volUpButton.setOnClickListener(v -> sendBleCommand("VOL+"));
        volDownButton.setOnClickListener(v -> sendBleCommand("VOL-"));
        vol15Button.setOnClickListener(v -> sendBleCommand("SETVOL 15"));
        playPauseButton.setOnClickListener(v -> sendBleCommand("PLAY 2"));
    }

    @Override
    public void onResume() {
        super.onResume();
        setupBleManager();

        View fragmentView = getView();
        if (fragmentView != null) {
            fragmentView.postDelayed(() -> sendBleCommand("LIST"), 1000);
        }
    }

    private void setupBleManager() {
        if (getActivity() instanceof MainActivity) {
            bleManager = ((MainActivity) getActivity()).getBleManager();
        }
    }

    private void sendBleCommand(String cmd) {
        if (bleManager != null) {
            Log.d("RadioFragment", "Próba wysłania: " + cmd);
            bleManager.sendCommand(cmd);
        } else {
            Toast.makeText(getContext(), "Błąd: Brak połączenia!", Toast.LENGTH_SHORT).show();
        }
    }
    public void updateStationName(String name) {
        if (stationNameTextView != null) {
            stationNameTextView.setText(name);
        }
    }
}