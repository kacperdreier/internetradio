package com.example.internetradio;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.internetradio.bluetooth.BleManager;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private BleManager bleManager;

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

        checkPermissions();

        bleManager = new BleManager(this, new BleManager.BleListener() {
            @Override
            public void onStatusUpdate(String message) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onDataReceived(String data) {
                runOnUiThread(() -> {
                    updateRadioFragmentUI(data);
                });
            }
        });
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
}