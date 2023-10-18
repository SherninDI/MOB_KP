package com.example.kp;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.Closeable;

public class BluetoothPairingHandler implements Closeable {
    private static final String TAG = "BluetoothPairingHandler";
    private final BluetoothAdapter bluetoothAdapter;
    private final BluetoothBroadcastReceiver bluetoothBroadcastReceiver;
    private final Context context;
    private BluetoothDevice boundingDevice;
    public BluetoothPairingHandler(Context context, BluetoothAdapter adapter, BluetoothDeviceDiscoveryListener listener) {
        this.context = context;
        this.bluetoothAdapter = adapter;
        this.bluetoothBroadcastReceiver = new BluetoothBroadcastReceiver(context, listener, this);
    }
    public boolean isBluetoothEnabled() {
        return bluetoothAdapter.isEnabled();
    }

    public void startDiscovery() {
        bluetoothBroadcastReceiver.onDeviceDiscoveryStarted();
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        if (!bluetoothAdapter.startDiscovery()) {
//            Toast.makeText(context, "Error while starting device discovery!", Toast.LENGTH_SHORT)
//                    .show();
//            Log.d(TAG, "StartDiscovery returned false. Maybe Bluetooth isn't on?");

            // Ends the discovery.
            bluetoothBroadcastReceiver.onDeviceDiscoveryEnd();
        }
    }
    public boolean isDiscovering() {
        return bluetoothAdapter.isDiscovering();
    }

    public void cancelDiscovery() {
        if(bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
            bluetoothBroadcastReceiver.onDeviceDiscoveryEnd();
        }
    }

    public boolean pair(BluetoothDevice device) {
        // Stops the discovery and then creates the pairing.
        if (bluetoothAdapter.isDiscovering()) {
            Log.d(TAG, "Bluetooth cancelling discovery.");
            bluetoothAdapter.cancelDiscovery();
        }
        Log.d(TAG, "Bluetooth bonding with device: " + deviceToString(device));
        boolean outcome = device.createBond();
        Log.d(TAG, "Bounding outcome : " + outcome);

        // If the outcome is true, we are bounding with this device.
        if (outcome) {
            this.boundingDevice = device;
        }
        return outcome;
    }
    public boolean isAlreadyPaired(BluetoothDevice device) {
        return bluetoothAdapter.getBondedDevices().contains(device);
    }

    public static String deviceToString(BluetoothDevice device) {
        return "[Address: " + device.getAddress() + ", Name: " + device.getName() + "]";
    }
    public int getPairingDeviceStatus() {
        if (this.boundingDevice == null) {
            throw new IllegalStateException("No device currently bounding");
        }
        int bondState = this.boundingDevice.getBondState();
        // If the new state is not BOND_BONDING, the pairing is finished, cleans up the state.
        if (bondState != BluetoothDevice.BOND_BONDING) {
            this.boundingDevice = null;
        }
        return bondState;
    }

    public boolean isPairingInProgress() {
        return this.boundingDevice != null;
    }

    public BluetoothDevice getBoundingDevice() {
        return boundingDevice;
    }

    public static String getDeviceName(BluetoothDevice device) {
        String deviceName = device.getName();
        if (deviceName == null) {
            deviceName = device.getAddress();
        }
        return deviceName;
    }

    @Override
    public void close() {
        this.bluetoothBroadcastReceiver.close();
    }
}
