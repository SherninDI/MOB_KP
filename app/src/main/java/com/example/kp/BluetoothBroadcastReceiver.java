package com.example.kp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class BluetoothBroadcastReceiver extends BroadcastReceiver {
    private final BluetoothDeviceDiscoveryListener listener;
    private final Context context;

    public BluetoothBroadcastReceiver(Context context, BluetoothDeviceDiscoveryListener listener, BluetoothPairingHandler bluetoothPairingHandler) {
        this.listener = listener;
        this.context = context;
        this.listener.setBluetoothPairingHandler(bluetoothPairingHandler);

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(this, filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case BluetoothDevice.ACTION_FOUND :
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                listener.onDeviceDiscovered(device);
                break;

            case BluetoothAdapter.ACTION_DISCOVERY_STARTED :
//                Log.e(TAG,"Start ");
                break;

            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                listener.onDeviceDiscoveryEnd();
//                Log.e(TAG,"Finish ");
                break;

            case BluetoothAdapter.ACTION_STATE_CHANGED:
//                listener.onBluetoothStatusChanged();
                break;

            case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                listener.onDevicePairingEnded();
                break;
            default:
                break;
        }

    }
    public void onDeviceDiscoveryStarted() {
        listener.onDeviceDiscoveryStarted();
    }

    public void onDeviceDiscoveryEnd() {
        listener.onDeviceDiscoveryEnd();
    }

    public void close() {
        context.unregisterReceiver(this);
    }
}
