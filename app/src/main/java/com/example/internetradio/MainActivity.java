package com.example.internetradio;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
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
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private BleManager bleManager;
    private StationViewModel viewModel;
    Set<String> beingProcessed = new HashSet<>();

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                Boolean scanGranted = permissions.getOrDefault(Manifest.permission.BLUETOOTH_SCAN, false);
                Boolean connectGranted = permissions.getOrDefault(Manifest.permission.BLUETOOTH_CONNECT, false);

                if (scanGranted != null && scanGranted && connectGranted != null && connectGranted) {
                    Toast.makeText(this, "Uprawnienia BT przyznane", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Aplikacja wymaga uprawnień Bluetooth!", Toast.LENGTH_LONG).show();
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
                runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
                if (message.contains("Połączenie gotowe") || message.contains("Connected")) {
                    Log.d("BLE_SYNC", "Połączono! Resetuję indeksy, aby wymusić wysyłkę ulubionych.");
                    viewModel.resetAllIndices();
                }
            }

            @Override
            public void onDataReceived(String data) {
                runOnUiThread(() -> updateRadioFragmentUI(data));
            }
        });

        checkPermissions();

        if (hasRequiredPermissions()) {
            connectWithDelay();
        }

        viewModel.getFavoriteStations().observe(this, favorites -> {
            if (bleManager != null && bleManager.isConnected() && favorites != null) {
                int delay = 500;

                for (RadioStation station : favorites) {
                    if (station.getEspIndex() == -1 && !beingProcessed.contains(station.getStationUuid())) {

                        beingProcessed.add(station.getStationUuid());
                        int finalIdx = viewModel.getNextEspIndex();

                        new android.os.Handler(getMainLooper()).postDelayed(() -> {
                            station.setEspIndex(finalIdx);
                            viewModel.update(station); // Zapis do bazy

                            String cleanName = station.getName().replace(" ", "_");
                            bleManager.sendCommand("ADD " + station.getUrl() + " " + cleanName);

                            Log.d("BLE_SYNC", "Wysłano ADD dla: " + cleanName + " na slot: " + finalIdx);
                        }, delay);

                        delay += 1200;
                    }
                }
            }
        });;
    }

    private void connectWithDelay() {
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (bleManager != null) {
                bleManager.connectToRadio();
            }
        }, 3000);
    }

    private boolean hasRequiredPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            return androidx.core.content.ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == android.content.pm.PackageManager.PERMISSION_GRANTED &&
                    androidx.core.content.ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == android.content.pm.PackageManager.PERMISSION_GRANTED;
        }
        return androidx.core.content.ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

    private void checkPermissions() {
        String[] permissions;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            permissions = new String[]{
                    android.Manifest.permission.BLUETOOTH_SCAN,
                    android.Manifest.permission.BLUETOOTH_CONNECT,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            };
        } else {
            permissions = new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            };
        }

        for (String permission : permissions) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, permission)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, permissions, 1);
                break;
            }
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
                androidx.fragment.app.Fragment currentFragment = navHostFragment
                        .getChildFragmentManager().getFragments().get(0);

                if (currentFragment instanceof com.example.internetradio.fragments.RadioFragment) {
                    ((com.example.internetradio.fragments.RadioFragment) currentFragment).updateStationName(radioData);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Błąd aktualizacji UI: " + e.getMessage());
        }
    }
    public BleManager getBleManager() {
        return bleManager;
    }
    private void syncFavoritesToEsp() {
        viewModel.getFavoriteStations().observe(this, favorites -> {
            if (favorites == null) return;

            int index = 1;
            for (RadioStation station : favorites) {
                if (index >= 10) break;

                String cleanName = station.getName().replace(" ", "_");
                String cmd = "ADD " + station.getUrl() + " " + cleanName;

                station.setEspIndex(index);
                viewModel.update(station);

                final int finalIndex = index;
                new android.os.Handler(getMainLooper()).postDelayed(() -> {
                    bleManager.sendCommand(cmd);
                }, index * 800);

                index++;
            }
        });
    }
}