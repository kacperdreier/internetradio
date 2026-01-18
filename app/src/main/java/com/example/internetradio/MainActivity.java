package com.example.internetradio;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.internetradio.bluetooth.BleManager;
import com.example.internetradio.data.RadioStation;
import com.example.internetradio.viewmodel.StationViewModel;
import com.google.android.material.navigation.NavigationView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private BleManager bleManager;
    private StationViewModel viewModel;
    private final Set<String> syncingUuids = new HashSet<>();

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                boolean allGranted = true;
                for (Boolean granted : permissions.values()) {
                    if (!granted) {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted) {
                    Toast.makeText(this, "Uprawnienia Bluetooth przyznane", Toast.LENGTH_SHORT).show();
                    connectWithDelay();
                } else {
                    Toast.makeText(this, "Aplikacja wymaga uprawnień do działania!", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(StationViewModel.class);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_radio_player, R.id.nav_station_list, R.id.nav_settings, R.id.nav_about)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        bleManager = new BleManager(this, new BleManager.BleListener() {
            @Override
            public void onStatusUpdate(String message) {
                runOnUiThread(() -> {
                    Log.d("BleStatus", message);
                    if (message.contains("Połączenie gotowe")) {
                        new android.os.Handler(getMainLooper()).postDelayed(() -> {
                            bleManager.sendCommand("CURRENT");
                        }, 1500);
                    }
                });
            }

            @Override
            public void onDataReceived(String data) {
                runOnUiThread(() -> updateRadioFragmentUI(data));
            }
        });

        checkPermissions();

        viewModel.getFavoriteStations().observe(this, favorites -> {
            if (bleManager != null && bleManager.isConnected() && favorites != null) {
                boolean needsSync = false;
                for (int i = 0; i < favorites.size(); i++) {
                    if (favorites.get(i).getEspIndex() != i) {
                        needsSync = true;
                        break;
                    }
                }

                if (needsSync) {
                    Log.d("BLE_SYNC", "Wykryto niespójność indeksów. Start Full Sync...");
                    performFullSync(favorites);
                }
            }
        });
    }

    private void performFullSync(List<RadioStation> favorites) {
        viewModel.resetAllIndices();

        for (int i = 0; i < 10; i++) {
            final int delay = i * 250;
            new android.os.Handler(getMainLooper()).postDelayed(() -> {
                bleManager.sendCommand("DELETE 0");
            }, delay);
        }

        new android.os.Handler(getMainLooper()).postDelayed(() -> {
            int currentDelay = 0;

            for (int i = 0; i < favorites.size(); i++) {
                if (i >= 10) break;

                final int slotIdx = i;
                final RadioStation station = favorites.get(i);
                final String cleanName = station.getName().replace(" ", "_");

                new android.os.Handler(getMainLooper()).postDelayed(() -> {
                    bleManager.sendCommand("ADD " + station.getUrl() + " " + cleanName);

                    station.setEspIndex(slotIdx);
                    viewModel.update(station);

                    Log.d("BLE_SYNC", "Dodano: " + cleanName + " na slot: " + slotIdx);
                }, currentDelay);

                currentDelay += 1200;
            }

            new android.os.Handler(getMainLooper()).postDelayed(() -> {
                bleManager.sendCommand("SAVE");
                Toast.makeText(this, "Lista w radiu zaktualizowana!", Toast.LENGTH_SHORT).show();
            }, currentDelay + 1000);

        }, 3000);
    }

    private void connectWithDelay() {
        new android.os.Handler(getMainLooper()).postDelayed(() -> {
            if (bleManager != null && !bleManager.isConnected()) {
                bleManager.connectToRadio();
            }
        }, 2000);
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
            });
        } else {
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void updateRadioFragmentUI(String radioData) {
        try {
            androidx.fragment.app.Fragment navHostFragment = getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment_content_main);

            if (navHostFragment != null) {
                List<androidx.fragment.app.Fragment> fragments = navHostFragment.getChildFragmentManager().getFragments();
                if (!fragments.isEmpty()) {
                    androidx.fragment.app.Fragment currentFragment = fragments.get(0);
                    if (currentFragment instanceof com.example.internetradio.fragments.RadioFragment) {
                        ((com.example.internetradio.fragments.RadioFragment) currentFragment).updateStationName(radioData);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Błąd aktualizacji UI: " + e.getMessage());
        }
    }

    public BleManager getBleManager() {
        return bleManager;
    }
}