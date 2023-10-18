package com.example.kp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;

import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import static android.app.PendingIntent.getActivity;
import static java.security.AccessController.getContext;


public class MainActivity extends AppCompatActivity implements DeviceListInteractionListener<BluetoothDevice>{

    private static String TAG = "Device";
    private final static int REQUEST_ENABLE_BT = 1;
    private Button btOn;
    private Button btOff;
    private FloatingActionButton discover;
    private RecyclerView rvDevices;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothPairingHandler bluetoothPairingHandler;
    private ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();

    private DeviceAdapter deviceAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btOn = findViewById(R.id.btOn);
        btOff = findViewById(R.id.btOff);
        discover = findViewById(R.id.discover);
        rvDevices = findViewById(R.id.device_list);

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        deviceAdapter = new DeviceAdapter(this);
        rvDevices.setLayoutManager(new LinearLayoutManager(this));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);


        btOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothOn();
            }
        });

        btOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothOff();
            }
        });

        discover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rvDevices.setAdapter(deviceAdapter);
                bluetoothPairingHandler = new BluetoothPairingHandler(getApplicationContext(), bluetoothAdapter, deviceAdapter);
                if (!bluetoothPairingHandler.isBluetoothEnabled()){
//                    bluetoothPairingHandler.turnOnBluetoothAndScheduleDiscovery();
                } else {
                    if (!bluetoothPairingHandler.isDiscovering()) {
                        bluetoothPairingHandler.startDiscovery();
                        Snackbar.make(v, "Поиск устройств...", Snackbar.LENGTH_LONG)
                                .show();
                    } else {
                        bluetoothPairingHandler.cancelDiscovery();
                        Snackbar.make(v, "Поиск устройств остановлен", Snackbar.LENGTH_LONG)
                                .show();
                    }
                }
//                discover();
            }
        });
    }

    private void bluetoothOn() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//            mBluetoothStatus.setText(getString(R.string.BTEnable));
            Toast.makeText(getApplicationContext(),"On",Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(),"Already ON", Toast.LENGTH_SHORT).show();
        }
    }

    private void bluetoothOff() {

        bluetoothAdapter.disable(); // turn off
//        mBluetoothStatus.setText(getString(R.string.sBTdisabl));
        Toast.makeText(getApplicationContext(),"Off", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void startLoading() {
        discover.setImageResource(R.drawable.ic_bluetooth_searching_white_24dp);
    }

    @Override
    public void endLoading(boolean partialResults) {
        if (!partialResults) {
            discover.setImageResource(R.drawable.ic_bluetooth_white_24dp);
        }
    }

    @Override
    public void onItemClick(BluetoothDevice device) {
        if (bluetoothPairingHandler.isAlreadyPaired(device)) {
            Log.d(TAG, "Device already paired!");
//            Toast.makeText(getActivity(), R.string.device_already_paired, Toast.LENGTH_SHORT).show();

            if (device != null) {
                String address = device.getAddress();
                Log.e(TAG, address);
                Intent toData = new Intent(getBaseContext(), DataActivity.class);
                toData.putExtra("device_address", address);
                startActivity(toData);

//                Intent intent = new Intent(getActivity().getBaseContext(), DataActivity.class);
//                getActivity().startActivity(intent);
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
        // Gets the message to print.
        if (error) {
            Log.e(TAG,"Failed pairing with device " + deviceName + "!");
        } else {
            Log.e(TAG,"Succesfully paired with device " + deviceName + "!");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
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