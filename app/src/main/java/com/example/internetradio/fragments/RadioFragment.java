package com.example.internetradio.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
    private Button volUpButton, volDownButton, vol15Button, connectButton, saveButton, refreshButton, eraseButton;
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
        eraseButton = view.findViewById(R.id.button_erase_esp);

        setupBleManager();

        connectButton.setOnClickListener(v -> {
            if (bleManager != null) bleManager.connectToRadio();
        });

        nextButton.setOnClickListener(v -> sendControlCommand("NEXT"));
        prevButton.setOnClickListener(v -> sendControlCommand("PREV"));
        volUpButton.setOnClickListener(v -> sendBleCommand("VOL+"));
        volDownButton.setOnClickListener(v -> sendBleCommand("VOL-"));
        vol15Button.setOnClickListener(v -> sendBleCommand("SETVOL 15"));

        saveButton.setOnClickListener(v -> {
            sendBleCommand("SAVE");
            Toast.makeText(getContext(), "Wysłano prośbę o zapis do pamięci ESP", Toast.LENGTH_SHORT).show();
        });

        eraseButton.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Wyczyścić pamięć radia?")
                    .setMessage("To usunie wszystkie zapisane stacje z pamięci trwałej ESP32. Lista w RAM pozostanie do restartu.")
                    .setPositiveButton("Tak, usuń", (dialog, which) -> {
                        sendBleCommand("ERASE");
                        Toast.makeText(getContext(), "Wyczyszczono pamięć Flash radia!", Toast.LENGTH_LONG).show();
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            sendBleCommand("LIST");
                        }, 1000);
                    })
                    .setNegativeButton("Anuluj", null)
                    .show();
        });

        refreshButton.setOnClickListener(v -> {
            sendBleCommand("CURRENT");
            new Handler(Looper.getMainLooper()).postDelayed(() -> sendBleCommand("LIST"), 500);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setupBleManager();

        if (bleManager != null && bleManager.isConnected()) {
            bleManager.sendCommand("CURRENT");

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (bleManager != null && bleManager.isConnected()) {
                    bleManager.sendCommand("LIST");
                }
            }, 500);
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
    private void sendControlCommand(String cmd) {
        if (bleManager != null && bleManager.isConnected()) {
            bleManager.sendCommand(cmd);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if(bleManager.isConnected()) {
                    bleManager.sendCommand("CURRENT");
                }
            }, 1000);
        } else {
            Toast.makeText(getContext(), "Błąd: Brak połączenia!", Toast.LENGTH_SHORT).show();
        }
    }
    public void updateStationName(String data) {
        if (getActivity() == null || currentStationTextView == null) return;

        getActivity().runOnUiThread(() -> {
            if (data.contains("\n") && (data.contains("0:") || data.contains("1:"))) {
                if (stationNameTextView != null) {
                    stationNameTextView.setText(data);
                    stationNameTextView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                }
            }
            else if (data.contains(";")) {
                String[] parts = data.split(";");
                if (currentStationTextView != null) {
                    currentStationTextView.setText(parts[0].replace("_", " ").trim());
                }
            }
            else {
                if (data.contains("PLAYING")) {
                    if (currentStationTextView != null)
                        currentStationTextView.setText(data.replace("PLAYING:", "").trim());
                }
            }
        });
    }
}