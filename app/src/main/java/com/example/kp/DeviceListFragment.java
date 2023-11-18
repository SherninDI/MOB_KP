package com.example.kp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.navigation.fragment.NavHostFragment;

public class DeviceListFragment extends Fragment implements DeviceListInteractionListener<BluetoothDevice>{
    private final String TAG = DeviceListFragment.class.getSimpleName();
    private BluetoothAdapter bluetoothAdapter;
    private DeviceAdapter deviceAdapter;
    private RecyclerView deviceList;
    private BluetoothPairingHandler bluetoothPairingHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device_list, container, false);
    }

    Button discover;
    Button connect;

    private String pairedDevice;

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        discover = view.findViewById(R.id.discover);
        connect = view.findViewById(R.id.connect);
        deviceList = view.findViewById(R.id.device_list);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        deviceAdapter = new DeviceAdapter(this);

        discover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceList.setAdapter(deviceAdapter);
                bluetoothPairingHandler = new BluetoothPairingHandler(getActivity(), bluetoothAdapter, deviceAdapter);
                if (!bluetoothPairingHandler.isBluetoothEnabled()){
                    bluetoothPairingHandler.turnOnBluetoothAndScheduleDiscovery();
                } else {
                    if (!bluetoothPairingHandler.isDiscovering()) {
                        bluetoothPairingHandler.startDiscovery();
                        Snackbar.make(view, "Поиск устройств...", Snackbar.LENGTH_LONG)
                                .show();
                    } else {
                        bluetoothPairingHandler.cancelDiscovery();
                        Snackbar.make(view, "Поиск устройств остановлен", Snackbar.LENGTH_LONG)
                                .show();
                    }
                }
            }
        });

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("address", pairedDevice);
                NavHostFragment.findNavController(DeviceListFragment.this)
                        .navigate(R.id.action_DeviceListFragment_to_ChatFragment, bundle);
            }
        });
    }

    @Override
    public void startLoading() {

    }

    @Override
    public void endLoading(boolean partialResults) {
        if (!partialResults) {

        }
    }

    @Override
    public void onItemClick(BluetoothDevice device) {
        if (bluetoothPairingHandler.isAlreadyPaired(device)) {
            Log.d(TAG, "Device already paired!");

            if (device != null) {
                pairedDevice = device.getAddress();
                Log.e(TAG, pairedDevice);
            }

        } else {
            Log.d(TAG, "Device not paired. Pairing.");
            boolean outcome = bluetoothPairingHandler.pair(device);

            // Prints a message to the user.
            String deviceName = BluetoothPairingHandler.getDeviceName(device);
            if (outcome) {
                // The pairing has started, shows a progress dialog.
                Log.d(TAG, "Showing pairing dialog");
//                bondingProgressDialog = ProgressDialog.show(this, "", "Pairing with device " + deviceName + "...", true, false);
            } else {
                Log.d(TAG, "Error while pairing with device " + deviceName + "!");
//                Toast.makeText(this, "Error while pairing with device " + deviceName + "!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void endLoadingWithDialog(boolean error, BluetoothDevice device) {
        String deviceName = BluetoothPairingHandler.getDeviceName(device);
        if (error) {
            Log.e(TAG,"Failed pairing with device " + deviceName + "!");
        } else {
            Log.e(TAG,"Succesfully paired with device " + deviceName + "!");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (bluetoothPairingHandler.isDiscovering()) {
            bluetoothPairingHandler.cancelDiscovery();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (this.bluetoothPairingHandler != null) {
            this.bluetoothPairingHandler.cancelDiscovery();
        }
    }

    @Override
    public void onDestroy() {
        bluetoothPairingHandler.close();
        super.onDestroy();
    }
}