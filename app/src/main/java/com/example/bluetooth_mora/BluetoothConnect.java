package com.example.bluetooth_mora;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothConnect {
    private final UUID serialPortUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter mBluetoothAdapter;
    private BluetoothServerSocket mServerSocket;

    private BluetoothDevice mSelectDevice;

    public static BluetoothSocket mServer_Socket;
    public static BluetoothSocket mClient_Socket;

    public static InputStream mServer_InputStream;
    public static InputStream mClient_InputStream;

    public static OutputStream mServer_OutputStream;
    public static OutputStream mClient_OutputStream;

    private AcceptThread mAcceptThread;

    public BluetoothConnect(BluetoothAdapter bluetoothAdapter) {
        this.mBluetoothAdapter = bluetoothAdapter;
    }

    public void startAcceptThread() {
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
    }

    public void cancelAcceptThread() {
        if (mAcceptThread != null) {
            try {
                mAcceptThread.join();
                mAcceptThread = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // 服務端接收資訊執行緒
    private class AcceptThread extends Thread {
        public AcceptThread() {
            try {
                // 通過UUID監聽請求,然後獲取到對應的服務端介面
                String NAME = "Bluetooth_Socket";
                mServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, serialPortUUID);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }

        public void run() {
            try {
                // 接收其客戶端的介面
                mClient_Socket = mServerSocket.accept();
                // 獲取到輸入流
                mClient_InputStream = mClient_Socket.getInputStream();
                mClient_OutputStream = mClient_Socket.getOutputStream();
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }
    }

    public void Connect(String address) {
        // 如果選擇裝置為空則代表還沒有選擇裝置
        if (mSelectDevice == null) {
            // 通過地址獲取到該裝置
            mSelectDevice = mBluetoothAdapter.getRemoteDevice(address);
        }

        try {
            // 獲取到服務端介面
            mServer_Socket = mSelectDevice.createRfcommSocketToServiceRecord(serialPortUUID);
            // 向服務端傳送連線
            mServer_Socket.connect();
            // 獲取到輸出流,向外寫資料
            mServer_OutputStream = mServer_Socket.getOutputStream();
            mServer_InputStream = mServer_Socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
