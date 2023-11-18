package com.example.kp;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.Closeable;
import java.security.Permissions;

import static android.support.v4.app.ActivityCompat.requestPermissions;

public class BluetoothPairingHandler implements Closeable {
    private final String TAG = BluetoothPairingHandler.class.getSimpleName();
    private final BluetoothAdapter bluetoothAdapter;
    private final BluetoothBroadcastReceiver bluetoothBroadcastReceiver;
    private final Activity activity;
    private boolean bluetoothDiscoveryScheduled;
    private BluetoothDevice boundingDevice;

    public BluetoothPairingHandler(Activity activity, BluetoothAdapter adapter, BluetoothDeviceDiscoveryListener listener) {
        this.activity = activity;
        this.bluetoothAdapter = adapter;
        this.bluetoothBroadcastReceiver = new BluetoothBroadcastReceiver(activity, listener, this);
    }

    public boolean isBluetoothEnabled() {
        return bluetoothAdapter.isEnabled();
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public void startDiscovery() {
        bluetoothBroadcastReceiver.onDeviceDiscoveryStarted();
        checkBTPermissions();
            if (bluetoothAdapter.isDiscovering()) {

                bluetoothAdapter.cancelDiscovery();
            }

            Log.d(TAG, "Bluetooth starting discovery.");
            if (!bluetoothAdapter.startDiscovery()) {
                Toast.makeText(activity, "Error while starting device discovery!", Toast.LENGTH_SHORT)
                        .show();
                Log.d(TAG, "StartDiscovery returned false. Maybe Bluetooth isn't on?");
                // Ends the discovery.
                bluetoothBroadcastReceiver.onDeviceDiscoveryEnd();
            }

    }

    public void turnOnBluetooth() {
        Log.d(TAG, "Enabling Bluetooth.");
        bluetoothBroadcastReceiver.onBluetoothTurningOn();
        bluetoothAdapter.enable();
    }

    public boolean pair(BluetoothDevice device) {

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

    @Override
    public void close() {
        this.bluetoothBroadcastReceiver.close();
    }

    public boolean isDiscovering() {
        return bluetoothAdapter.isDiscovering();
    }

    public void cancelDiscovery() {
        bluetoothAdapter.cancelDiscovery();
        bluetoothBroadcastReceiver.onDeviceDiscoveryEnd();
    }

    public void turnOnBluetoothAndScheduleDiscovery() {
        this.bluetoothDiscoveryScheduled = true;
        turnOnBluetooth();
    }

    public void onBluetoothStatusChanged() {
        // Does anything only if a device discovery has been scheduled.
        if (bluetoothDiscoveryScheduled) {

            int bluetoothState = bluetoothAdapter.getState();
            switch (bluetoothState) {
                case BluetoothAdapter.STATE_ON:
                    // Bluetooth is ON.

                    Log.d(TAG, "Bluetooth succesfully enabled, starting discovery");
                    startDiscovery();
                    // Resets the flag since this discovery has been performed.
                    bluetoothDiscoveryScheduled = false;
                    break;
                case BluetoothAdapter.STATE_OFF:
                    // Bluetooth is OFF.
                    Log.d(TAG, "Error while turning Bluetooth on.");
                    Toast.makeText(activity, "Error while turning Bluetooth on.", Toast.LENGTH_SHORT);
                    bluetoothDiscoveryScheduled = false;
                    break;
                default:
                    break;
            }
        }
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


    public static String getDeviceName(BluetoothDevice device) {
        String deviceName = device.getName();
        if (deviceName == null) {
            deviceName = device.getAddress();
        }
        return deviceName;
    }

    public boolean isPairingInProgress() {
        return this.boundingDevice != null;
    }

    public BluetoothDevice getBoundingDevice() {
        return boundingDevice;
    }

    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = activity.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += activity.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
            }
        }else{
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }
}
