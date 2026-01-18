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

    private ImageButton prevButton, nextButton;
    private Button volUpButton, volDownButton, vol15Button, connectButton, saveButton, refreshButton;
    private BleManager bleManager;
    private TextView stationNameTextView;
    private TextView currentStationTextView;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_radio, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        stationNameTextView = view.findViewById(R.id.station_name_text);
        currentStationTextView = view.findViewById(R.id.current_station_display);
        prevButton = view.findViewById(R.id.button_prev_station);
        nextButton = view.findViewById(R.id.button_next_station);
        volUpButton = view.findViewById(R.id.button_vol_plus);
        volDownButton = view.findViewById(R.id.button_vol_minus);
        vol15Button = view.findViewById(R.id.button_vol_15);
        connectButton = view.findViewById(R.id.button_ble_connect);
        saveButton = view.findViewById(R.id.button_save_esp);
        refreshButton = view.findViewById(R.id.button_refresh_status);

        setupBleManager();

        connectButton.setOnClickListener(v -> {
            if (bleManager != null) bleManager.connectToRadio();
        });

        nextButton.setOnClickListener(v -> sendBleCommand("NEXT"));
        prevButton.setOnClickListener(v -> sendBleCommand("PREV"));
        volUpButton.setOnClickListener(v -> sendBleCommand("VOL+"));
        volDownButton.setOnClickListener(v -> sendBleCommand("VOL-"));
        vol15Button.setOnClickListener(v -> sendBleCommand("SETVOL 15"));

        saveButton.setOnClickListener(v -> {
            sendBleCommand("SAVE");
            Toast.makeText(getContext(), "Wysłano prośbę o zapis do pamięci ESP", Toast.LENGTH_SHORT).show();
        });

        refreshButton.setOnClickListener(v -> sendBleCommand("CURRENT"));
    }

    @Override
    public void onResume() {
        super.onResume();
        setupBleManager();

        if (bleManager != null && bleManager.isConnected()) {
            View fragmentView = getView();
            if (fragmentView != null) {
                fragmentView.postDelayed(() -> sendBleCommand("CURRENT"), 1000);
            }
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
    public void updateStationName(String data) {
        if (getActivity() == null || currentStationTextView == null) return;

        getActivity().runOnUiThread(() -> {
            if (data.contains(";")) {
                String[] parts = data.split(";");
                String name = parts[0].replace("_", " ").trim();
                currentStationTextView.setText(name);
            } else {
                String cleanName = data.replace("PLAYING:", "").replace("_", " ").trim();
                currentStationTextView.setText(cleanName);
            }

            if (stationNameTextView != null) {
                stationNameTextView.setText("Odebrano dane z ESP32");
            }
        });
    }
}