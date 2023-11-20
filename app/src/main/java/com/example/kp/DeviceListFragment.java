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
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.navigation.fragment.NavHostFragment;
import org.w3c.dom.Text;

public class DeviceListFragment extends Fragment implements DeviceListInteractionListener<BluetoothDevice>{
    private final String TAG = DeviceListFragment.class.getSimpleName();
    private BluetoothAdapter bluetoothAdapter;
    private DeviceAdapter deviceAdapter;
    private RecyclerView deviceList;
    private BluetoothPairingHandler bluetoothPairingHandler;

    Context _context;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    //Device is in Discoverable Mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled.");
                        break;
                    //Device not in discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting....");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected.");
                        break;
                }

            }
        }
    };

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
    Button makeDiscoverable;

    TextView status;

    private String pairedDevice;

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        discover = view.findViewById(R.id.discover);
        connect = view.findViewById(R.id.connect);
        makeDiscoverable = view.findViewById(R.id.make_discoverable);
        status = (TextView)getActivity().findViewById(R.id.status);
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

                        Snackbar.make(view, R.string.status_start_discovery, Snackbar.LENGTH_LONG)
                                .show();
                    } else {
                        bluetoothPairingHandler.cancelDiscovery();
                        Snackbar.make(view, R.string.status_finish_discovery, Snackbar.LENGTH_LONG)
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

        makeDiscoverable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivity(discoverableIntent);

            }
        });
    }

    @Override
    public void startLoading() {
        status.setText(getString(R.string.status_start_discovery));
        discover.setText(R.string.finish_discover);
    }

    @Override
    public void endLoading(boolean partialResults) {
        if (!partialResults) {
            status.setText(getString(R.string.status_finish_discovery));
            discover.setText(R.string.start_discover);
        }
    }

    @Override
    public void onItemClick(BluetoothDevice device) {
        connect.setEnabled(true);
        if (bluetoothPairingHandler.isAlreadyPaired(device)) {
            if (device != null) {
                pairedDevice = device.getAddress();
                status.setText(getString(R.string.status_ready_for_connect, device.getName()));
            }

        } else {
            String deviceName = BluetoothPairingHandler.getDeviceName(device);
            status.setText(getString(R.string.status_pairing, deviceName));
            boolean outcome = bluetoothPairingHandler.pair(device);

            if (outcome) {
                status.setText(getString(R.string.status_pairing_done, deviceName));


            } else {
                status.setText(getString(R.string.status_pairing_not_done, deviceName));
            }
        }
    }

    @Override
    public void endLoadingWithDialog(boolean error, BluetoothDevice device) {
        if (error) {
            status.setText(getString(R.string.status_pairing_not_done, device.getName()));
        } else {
            status.setText(getString(R.string.status_pairing_done, device.getName()));
            deviceList.setAdapter(null);
            deviceAdapter.setUnselected();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (bluetoothPairingHandler != null) {
            if (bluetoothPairingHandler.isDiscovering()) {
                bluetoothPairingHandler.cancelDiscovery();
            }
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
    public void onAttach(Context context)
    {
        super.onAttach(context);
        _context = context;
    }

    @Override
    public void onPause() {
        super.onPause();
        _context.unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        _context.registerReceiver(broadcastReceiver,intentFilter);
    }

    @Override
    public void onDestroy() {
        bluetoothPairingHandler.close();
        super.onDestroy();
    }
}