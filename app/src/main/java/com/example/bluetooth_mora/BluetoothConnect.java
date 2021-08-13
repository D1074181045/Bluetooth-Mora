package com.example.bluetooth_mora;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothConnect {
    // UUID,藍芽建立連結需要的
    private final UUID serialPortUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // 獲取到的藍芽介面卡
    private final BluetoothAdapter mBluetoothAdapter;

    // 獲取到的介面
    public static BluetoothSocket mServer_Socket, mClient_Socket;
    // 獲取到的輸入流
    public static InputStream mServer_InputStream, mClient_InputStream;
    // 獲取到的輸出流
    public static OutputStream mServer_OutputStream, mClient_OutputStream;

    private boolean readerStop;

    private ReceivedDelegate mReceiveDelegate;

    public interface ReceivedDelegate {
        void ReceivedServer(String value);
        void ReceivedClient(String value);
    }

    public BluetoothConnect(BluetoothAdapter bluetoothAdapter) {
        this.mBluetoothAdapter = bluetoothAdapter;
    }

    public void setDelegate(ReceivedDelegate delegate) {
        mReceiveDelegate = delegate;
    }

    public void StopReader() {
        readerStop = true;
    }

    // 接收服務端資料
    public final Thread ReceiveServerThread = new Thread(new Runnable() {
        @Override
        public void run() {
            readerStop = false;

            while (!readerStop) {
                try {
                    if (mServer_InputStream == null)
                        continue;
                    if (mServer_InputStream.available() <= 0)
                        continue;

                    BufferedReader bufferedInputStream = new BufferedReader(new InputStreamReader(mServer_InputStream));
                    String value = bufferedInputStream.readLine();

                    if (value.length() > 0) {
                        if (mReceiveDelegate != null)
                            mReceiveDelegate.ReceivedServer(value);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    // 接收客戶端資料
    public final Thread ReceiveClientThread = new Thread(new Runnable() {
        @Override
        public void run() {
            readerStop = false;

            while (!readerStop) {
                try {
                    if (mClient_InputStream == null)
                        continue;
                    if (mClient_InputStream.available() <= 0)
                        continue;

                    BufferedReader bufferedInputStream = new BufferedReader(new InputStreamReader(mClient_InputStream));
                    String value = bufferedInputStream.readLine();

                    if (value.length() > 0) {
                        if (mReceiveDelegate != null)
                            mReceiveDelegate.ReceivedClient(value);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    public void startAcceptThread() {
        AcceptThread mAcceptThread = new AcceptThread();
        mAcceptThread.start();
    }

    // 服務端接收資訊執行緒
    private class AcceptThread extends Thread {
        // 服務端介面
        private BluetoothServerSocket mServerSocket;

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
                mClient_OutputStream = mClient_Socket.getOutputStream();
                mClient_InputStream = mClient_Socket.getInputStream();
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }
    }

    public void Connect(String address) {
        try {
            // 選中傳送資料的藍芽裝置
            BluetoothDevice mSelectDevice = mBluetoothAdapter.getRemoteDevice(address);
            mServer_Socket = mSelectDevice.createRfcommSocketToServiceRecord(serialPortUUID);
            // 向服務端傳送連線
            mServer_Socket.connect();
            mServer_OutputStream = mServer_Socket.getOutputStream();
            mServer_InputStream = mServer_Socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
