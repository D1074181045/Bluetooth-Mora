package com.example.bluetooth_mora;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Set;

public class BluetoothViewAdapter extends RecyclerView.Adapter<BluetoothViewAdapter.ViewHolder> {
    private final BluetoothAdapter mBluetoothAdapter;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public static BluetoothDelegate mDelegate;

        public interface BluetoothDelegate {
            void BluetoothBtnConfirmOnClick(View view, BluetoothDevice device);
        }

        private final ImageView icon;
        private final TextView textName, textAddress;
        private final Button btnConfirm;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            icon = itemView.findViewById(R.id.icon);
            textName = itemView.findViewById(R.id.textName);
            textAddress = itemView.findViewById(R.id.textAddress);
            btnConfirm = itemView.findViewById(R.id.btnConfirm);
        }

        void loadDevice(@NonNull BluetoothDevice device) {
            String name = device.getName();
            if (name == null) name = "裝置名稱未顯示";

            icon.setImageResource(R.drawable.ic_bluetooth_black_24dp);
            textName.setText(name);
            textAddress.setText(device.getAddress());
            btnConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mDelegate != null) mDelegate.BluetoothBtnConfirmOnClick(view, device);
                }
            });
        }
    }

    public BluetoothViewAdapter(BluetoothAdapter bluetoothAdapter) {
        this.mBluetoothAdapter = bluetoothAdapter;
    }

    public void setDelegate(ViewHolder.BluetoothDelegate delegate) {
        ViewHolder.mDelegate = delegate;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bluetooth_device_item, parent, false);
        return new BluetoothViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BluetoothViewAdapter.ViewHolder holder, int position) {
        Set<BluetoothDevice> pairedDevices = this.mBluetoothAdapter.getBondedDevices();
        holder.loadDevice(pairedDevices.toArray(new BluetoothDevice[0])[position]);
    }

    @Override
    public int getItemCount() {
        return this.mBluetoothAdapter.getBondedDevices().size();
    }
}