package com.example.kp;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;
import android.os.Handler;

public class BluetoothConnectionService {
    private final String TAG = BluetoothConnectionService.class.getSimpleName();
    private static final String NAME_INSECURE = "BluetoothChatInsecure";
    private static final UUID MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private final BluetoothAdapter bluetoothAdapter;
    Context context;

    private AcceptThread insecureAcceptThread;

    private ConnectThread connectThread;
    private BluetoothDevice bluetoothDevice;
    private ConnectedThread connectedThread;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;

    private final Handler handler;
    private int bluetoothState;

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    public BluetoothConnectionService(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        start();
    }

    private synchronized void setState(int state) {
        bluetoothState = state;
        handler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    public synchronized int getState() {
        return bluetoothState;
    }

    private class AcceptThread extends Thread {

        // The local server socket
        private final BluetoothServerSocket bluetoothServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try{
                tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME_INSECURE, MY_UUID_INSECURE);

                Log.d(TAG, "AcceptThread: Setting up Server using: " + MY_UUID_INSECURE);
            }catch (IOException e){
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage() );
            }

            bluetoothServerSocket = tmp;
        }

        public void run(){
            Log.d(TAG, "run: AcceptThread Running.");

            BluetoothSocket socket = null;

            try{

                Log.d(TAG, "run: RFCOM server socket start.....");

                socket = bluetoothServerSocket.accept();

                Log.d(TAG, "run: RFCOM server socket accepted connection.");

            }catch (IOException e){
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage() );
            }

            //talk about this is in the 3rd
            if(socket != null){
                connected(socket, bluetoothDevice);
                Message msg = handler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
                Bundle bundle = new Bundle();
                bundle.putString(Constants.DEVICE_NAME, bluetoothDevice.getName());
                msg.setData(bundle);
                handler.sendMessage(msg);
                setState(STATE_CONNECTED);
            }

            Log.i(TAG, "END AcceptThread ");
        }

        public void cancel() {
            Log.d(TAG, "cancel: Canceling AcceptThread.");
            try {
                bluetoothServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed. " + e.getMessage() );
            }
        }

    }

    private class ConnectThread extends Thread {
        private BluetoothSocket bluetoothSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectThread: started.");
            bluetoothDevice = device;
            deviceUUID = uuid;
        }

        public void run(){
            BluetoothSocket tmp = null;
            Log.i(TAG, "RUN ConnectThread ");

            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: "
                        +MY_UUID_INSECURE );
                tmp = bluetoothDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket " + e.getMessage());
            }

            bluetoothSocket = tmp;

            // Always cancel discovery because it will slow down a connection
            bluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket

            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                bluetoothSocket.connect();
                Message msg = handler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
                Bundle bundle = new Bundle();
                bundle.putString(Constants.DEVICE_NAME, bluetoothDevice.getName());
                msg.setData(bundle);
                handler.sendMessage(msg);
                setState(STATE_CONNECTED);
                Log.d(TAG, "run: ConnectThread connected.");
            } catch (IOException e) {
                // Close the socket
                try {
                    bluetoothSocket.close();
                    Log.d(TAG, "run: Closed Socket.");
                } catch (IOException e1) {
                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket " + e1.getMessage());
                }
                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + MY_UUID_INSECURE );
            }

            //will talk about this in the 3rd video
            connected(bluetoothSocket, bluetoothDevice);
        }
        public void cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket.");
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());
            }
        }
    }

    public synchronized void start() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        if (insecureAcceptThread == null) {
            insecureAcceptThread = new AcceptThread();
            insecureAcceptThread.start();
            setState(STATE_LISTEN);
        }
    }


    public void startClient(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startClient: Started.");

        mProgressDialog = ProgressDialog.show(context,"Connecting Bluetooth"
                ,"Please Wait...",true);

        connectThread = new ConnectThread(device, uuid);
        connectThread.start();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            bluetoothSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try{
                mProgressDialog.dismiss();
            }catch (NullPointerException e){
                e.printStackTrace();
            }


            try {
                tmpIn = bluetoothSocket.getInputStream();
                tmpOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[1024];  // buffer store for the stream

            int bytes;

            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {
                        handler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer)
                                .sendToTarget();
                    }

                } catch (IOException e) {
                    Message msg = handler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.DEVICE_NAME, bluetoothDevice.getName());
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                    setState(STATE_CONNECTING);
                    Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage() );
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
                handler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, bytes)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage() );
            }
        }

        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) { }
        }
    }

    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice) {
        Log.d(TAG, "connected: Starting.");

        connectedThread = new ConnectedThread(mmSocket);
        connectedThread.start();

        Message msg = handler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DEVICE_NAME, mmDevice.getName());
        msg.setData(bundle);
        handler.sendMessage(msg);

    }

    public  void stop() {
        if (connectThread != null) {
            connectThread.cancel(); connectThread = null;}
        if (connectedThread != null) {
            connectedThread.cancel(); connectedThread = null;}
        setState(STATE_NONE);
    }


    public void write(byte[] out) {
        ConnectedThread r;
        connectedThread.write(out);
    }
}
