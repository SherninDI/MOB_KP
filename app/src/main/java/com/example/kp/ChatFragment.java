package com.example.kp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.nio.charset.Charset;
import java.util.UUID;


public class ChatFragment extends Fragment {
    private final String TAG = ChatFragment.class.getSimpleName();

    BluetoothConnectionService bluetoothConnection;
    BluetoothDevice btDevice;

    Button send;
    EditText sendText;
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        send =view.findViewById(R.id.send);
        sendText = view.findViewById(R.id.message);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] bytes = sendText.getText().toString().getBytes(Charset.defaultCharset());
                bluetoothConnection.write(bytes);
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        Bundle args = getArguments();
        if (args != null) {
            String address = args.getString("address");
            Log.e(TAG,address);
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            btDevice = bluetoothAdapter.getRemoteDevice(address);
            bluetoothConnection = new BluetoothConnectionService(getContext());
            startConnection();
        }
    }



    public void startConnection(){
        startBTConnection(btDevice,MY_UUID_INSECURE);
    }


    public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startBTConnection: Initializing RFCOM Bluetooth Connection.");

        bluetoothConnection.startClient(device,uuid);
    }


}