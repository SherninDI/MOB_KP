package com.example.kp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class DataActivity extends AppCompatActivity {
    private static String TAG = "Data";
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    private String connectedDeviceName = null;
    private BluetoothConnectHandler bluetoothConnectHandler = null;

    // State variables
    private boolean paused = false;
    private boolean connected = false;
    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;

    EditText editText;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        editText = findViewById(R.id.editText);
        button = findViewById(R.id.button);

        Bundle args = getIntent().getExtras();
        String device_address = args.getString("device_address");
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter != null) {

            bluetoothConnectHandler = new BluetoothConnectHandler(bluetoothHandler, bluetoothAdapter);
            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(device_address);
            Log.d(TAG, bluetoothDevice.getName());
            bluetoothConnectHandler.connect(bluetoothDevice);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                byte[] msg = editText.getText().toString().getBytes(StandardCharsets.UTF_8);
                sendMessage(msg);
            }
        });


    }

    // Функция отправки сообщения
    private void sendMessage(byte[] message) {
        // Check that we're actually connected before trying anything
        if (bluetoothConnectHandler.getState() != BluetoothConnectHandler.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(), "BT NOT CON", Toast.LENGTH_SHORT).show();
            return;
        }
        if(message.length > 0) {
            bluetoothConnectHandler.write(message);
        }
    }

    private final Handler bluetoothHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (message.arg1) {
                        case BluetoothConnectHandler.STATE_CONNECTED:
                            connected = true;
                            Log.e(TAG, "STATE_CONNECTED");
                            if (connected) {


                            }

                            break;
                        case BluetoothConnectHandler.STATE_CONNECTING:
                            Log.e(TAG, "STATE_CONNECTING");
                            break;
                        case BluetoothConnectHandler.STATE_NONE:
                            connected = false;
                            Log.e(TAG, "STATE_NONE");
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    Log.e(TAG, bytesToHex((byte[])message.obj));
                    break;
                case MESSAGE_READ:
                    if (paused) break;
                    byte[] readBuf = (byte[]) message.obj;
                    System.out.println("READ  " + bytesToHex(readBuf) + " len  " +readBuf.length);
//                    if (readBuf.length > 22) {
//                        try {
//                            dataBuffer.write(readBuf);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    } else {
//                        System.out.println("READ  " + bytesToHex(readBuf) + " len  " +readBuf.length);
//                    }
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    connectedDeviceName = message.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + connectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), message.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });

    public static String bytesToHex(byte[] byteArray)
    {
        String hex = "";
        // Iterating through each byte in the array
        for (byte i : byteArray) {
            hex += String.format("%02X", i);
            hex += " ";
        }
        return hex;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bluetoothConnectHandler != null)
        {
            bluetoothConnectHandler.stop();
        }
    }
}