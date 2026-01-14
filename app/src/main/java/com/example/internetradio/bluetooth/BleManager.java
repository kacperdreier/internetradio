package com.example.internetradio.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.util.Log;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@SuppressLint("MissingPermission")
public class BleManager {
    private static final String TAG = "BleManager";
    private static final String TARGET_DEVICE_NAME = "ESP32-Radio";

    private static final UUID SERVICE_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    private static final UUID CHARACTERISTIC_RX_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    private static final UUID CHARACTERISTIC_TX_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public interface BleListener {
        void onStatusUpdate(String message);
        void onDataReceived(String data);
    }

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic commandCharacteristic;
    private final Context context;
    private final BleListener listener;
    private BluetoothGattCharacteristic rxCharacteristic;
    private BluetoothGattCharacteristic txCharacteristic;

    public BleManager(Context context, BleListener listener) {
        this.context = context;
        this.listener = listener;
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter != null) {
                bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            }
        }
    }

    public void connectToRadio() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            listener.onStatusUpdate("Błąd: Włącz Bluetooth!");
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (TARGET_DEVICE_NAME.equals(device.getName())) {
                listener.onStatusUpdate("Znaleziono w sparowanych, łączę...");
                connectToDevice(device);
                return;
            }
        }

        listener.onStatusUpdate("Szukam ESP32-Radio w okolicy...");
        startScan();
    }

    public void startScan() {
        if (bluetoothLeScanner == null) return;

        ScanFilter filter = new ScanFilter.Builder().setDeviceName(TARGET_DEVICE_NAME).build();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        bluetoothLeScanner.startScan(Collections.singletonList(filter), settings, scanCallback);
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            Log.d(TAG, "Skaner znalazł: " + device.getName());
            bluetoothLeScanner.stopScan(this);
            connectToDevice(device);
        }
    };

    private void connectToDevice(BluetoothDevice device) {
        bluetoothGatt = device.connectGatt(context, false, gattCallback);
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                listener.onStatusUpdate("Połączono! Konfiguruję...");
                gatt.requestMtu(512);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                listener.onStatusUpdate("Rozłączono z ESP32");
                commandCharacteristic = null;
                bluetoothGatt = null;
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            gatt.discoverServices();
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(SERVICE_UUID);
                if (service != null) {
                    rxCharacteristic = service.getCharacteristic(CHARACTERISTIC_RX_UUID);
                    txCharacteristic = service.getCharacteristic(CHARACTERISTIC_TX_UUID);

                    if (txCharacteristic != null) {
                        gatt.setCharacteristicNotification(txCharacteristic, true);
                        BluetoothGattDescriptor descriptor = txCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
                        if (descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor);
                        }
                    }
                    listener.onStatusUpdate("Połączenie gotowe!");
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (CHARACTERISTIC_TX_UUID.equals(characteristic.getUuid())) {
                byte[] data = characteristic.getValue();
                if (data != null && data.length > 0) {
                    String receivedText = new String(data);
                    Log.d(TAG, "Odebrano z ESP32: " + receivedText);

                    if (listener != null) {
                        listener.onDataReceived(receivedText);
                    }
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Deskryptor powiadomień zapisany pomyślnie");
            }
        }
    };

    public void sendCommand(String cmd) {
        if (bluetoothGatt != null && rxCharacteristic != null) {
            rxCharacteristic.setValue(cmd.getBytes());
            bluetoothGatt.writeCharacteristic(rxCharacteristic);
            Log.d(TAG, "Wysłano: " + cmd);
        } else {
            listener.onStatusUpdate("Błąd: Brak połączenia!");
        }
    }
}