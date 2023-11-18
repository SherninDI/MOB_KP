package com.example.kp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> implements BluetoothDeviceDiscoveryListener {
    private static String TAG = "DeviceAdapter";
    private final List<BluetoothDevice> devices;
    private BluetoothPairingHandler bluetoothPairingHandler;
    DeviceListInteractionListener<BluetoothDevice> listener;
    int selected_position = -1;

    DeviceAdapter(DeviceListInteractionListener<BluetoothDevice> listener) {
        this.devices = new ArrayList<>();
        this.listener = listener;
    }
    @Override
    public DeviceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_device, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DeviceAdapter.ViewHolder holder, int position) {
        BluetoothDevice device = devices.get(position);
        holder.itemImage.setImageResource(getDeviceIcon(device));
        holder.itemName.setText(device.getName());
        holder.itemAddress.setText(device.getAddress());
        holder.itemView.setBackgroundColor(selected_position == position ? Color.GREEN : Color.TRANSPARENT);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    if (holder.getAdapterPosition() == RecyclerView.NO_POSITION) return;
                    notifyItemChanged(selected_position);
                    selected_position = holder.getAdapterPosition();
                    notifyItemChanged(selected_position);
                    listener.onItemClick(device);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView itemImage;
        final TextView itemName, itemAddress;
        ViewHolder(View view){
            super(view);
            itemImage = view.findViewById(R.id.device_icon);
            itemName = view.findViewById(R.id.tvDeviceName);
            itemAddress = view.findViewById(R.id.tvDeviceAddress);
        }
    }

    @Override
    public void onDeviceDiscovered(BluetoothDevice device) {
        listener.endLoading(true);
        if (!devices.contains(device)) {
            devices.add(device);
            notifyDataSetChanged();
        }
    }

    @Override
    public void onDeviceDiscoveryStarted() {
        cleanView();
        listener.startLoading();
    }

    public void cleanView() {
        devices.clear();
        notifyDataSetChanged();
    }

    @Override
    public void onDeviceDiscoveryEnd() {
        listener.endLoading(false);
    }
    @Override
    public void setBluetoothPairingHandler(BluetoothPairingHandler bluetoothPairingHandler) {
        this.bluetoothPairingHandler = bluetoothPairingHandler;
    }

    @Override
    public void onBluetoothStatusChanged() {
        bluetoothPairingHandler.onBluetoothStatusChanged();
    }

    @Override
    public void onBluetoothTurningOn() {
        listener.startLoading();
    }

    private int getDeviceIcon(BluetoothDevice device) {
        if (bluetoothPairingHandler.isAlreadyPaired(device)) {
            return R.drawable.ic_bluetooth_connected_black_24dp;
        } else {
            return R.drawable.ic_bluetooth_black_24dp;
        }
    }

    @Override
    public void onDevicePairingEnded() {
        if (bluetoothPairingHandler.isPairingInProgress()) {
            BluetoothDevice device = bluetoothPairingHandler.getBoundingDevice();
            switch (bluetoothPairingHandler.getPairingDeviceStatus()) {
                case BluetoothDevice.BOND_BONDING:
                    // Still pairing, do nothing.
                    break;
                case BluetoothDevice.BOND_BONDED:
                    // Successfully paired.
                    listener.endLoadingWithDialog(false, device);

                    // Updates the icon for this element.
                    notifyDataSetChanged();
                    break;
                case BluetoothDevice.BOND_NONE:
                    // Failed pairing.
                    listener.endLoadingWithDialog(true, device);
                    break;
            }
        }
    }
}

