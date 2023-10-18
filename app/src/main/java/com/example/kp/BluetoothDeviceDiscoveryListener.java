package com.example.kp;

import android.bluetooth.BluetoothDevice;

public interface BluetoothDeviceDiscoveryListener {
    void onDeviceDiscovered(BluetoothDevice device);

    void onDeviceDiscoveryStarted();
    void onDeviceDiscoveryEnd();

    void setBluetoothPairingHandler(BluetoothPairingHandler bluetooth);

//    void onBluetoothStatusChanged();
//    void onBluetoothTurningOn();
    void onDevicePairingEnded();

}
