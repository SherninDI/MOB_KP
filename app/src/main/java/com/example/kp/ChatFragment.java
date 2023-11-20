package com.example.kp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;


public class ChatFragment extends Fragment {
    private final String TAG = ChatFragment.class.getSimpleName();
    private String connectedDeviceName = null;
    BluetoothConnectionService bluetoothConnection;
    BluetoothDevice btDevice;
    BluetoothAdapter bluetoothAdapter;
    Button send;
    EditText sendText;
    TextView status;
    TextView chat;

    RecyclerView messageList;
    MessageAdapter messageAdapter;
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
        send = view.findViewById(R.id.send);
        sendText = view.findViewById(R.id.message);
        messageList = view.findViewById(R.id.message_list);
//        chat = view.findViewById(R.id.chat);
        status = (TextView)getActivity().findViewById(R.id.status);

        messageAdapter = new MessageAdapter(getContext());
        messageList.setAdapter(messageAdapter);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sendText.getText().length() != 0) {
                    byte[] bytes = sendText.getText().toString().getBytes(Charset.defaultCharset());
                    bluetoothConnection.write(bytes);
                    sendText.setText("");
                }

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
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            btDevice = bluetoothAdapter.getRemoteDevice(address);
            bluetoothConnection = new BluetoothConnectionService(getContext(), handler);
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

    private final Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case  Constants.MESSAGE_DEVICE_NAME:
                    connectedDeviceName = message.getData().getString(Constants.DEVICE_NAME);
                    break;
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (message.arg1) {
                        case BluetoothConnectionService.STATE_CONNECTED:
                            send.setEnabled(true);
                            sendText.setEnabled(true);
                            status.setText(getString(R.string.status_connect, connectedDeviceName));
                            break;
                        case BluetoothConnectionService.STATE_LISTEN:

                            break;
                        case BluetoothConnectionService.STATE_CONNECTING:
                            send.setEnabled(false);
                            sendText.setEnabled(false);
                            status.setText(getString(R.string.status_waiting_for_connect, connectedDeviceName));

                            break;
                        case BluetoothConnectionService.STATE_NONE:

                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) message.obj;
                    String text = new String(writeBuf, Charset.defaultCharset());

                    String out = getString(R.string.message, bluetoothAdapter.getName(), text);
                    Messages msgOut = new MessageOut(out);
                    messageAdapter.addMessage(msgOut);
                    messageList.smoothScrollToPosition(0);
//                    chat.append(out);
                    Log.d(TAG, "write: Writing to outputstream: " + text);
                    break;
                case  Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) message.obj;
                    int bytes =  message.arg1;
                    String incomingMessage = new String(readBuf, 0, bytes);
                    String in = getString(R.string.message, connectedDeviceName, incomingMessage);
                    Messages msgIn = new MessageIn(in);
                    messageAdapter.addMessage(msgIn);
                    messageList.smoothScrollToPosition(0);
//                    chat.append(in);
                    Log.d(TAG, "InputStream: " + incomingMessage);
                    break;
                case  Constants.MESSAGE_TOAST:

                    break;
            }
            return false;
        }
    });


}