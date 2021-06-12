package com.example.bluetooth_mora;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

public class MatchActivity extends AppCompatActivity implements BluetoothViewAdapter.ViewHolder.BluetoothDelegate {
    private BluetoothViewAdapter bluetoothViewAdapter;
    // 獲取到藍芽介面卡
    private BluetoothAdapter bluetoothAdapter;
    // 選中傳送資料的藍芽裝置,全域性變數,否則連線在方法執行完就結束了

    public BluetoothConnect mBluetoothConnect;
    private boolean readerStop;

    public void restartApp() {
        Intent intent = getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    // 接收服務端資料
    private final Thread ReceiveServerThread = new Thread(new Runnable() {
        @Override
        public void run() {
            readerStop = false;

            while (!readerStop) {
                try {
                    if (BluetoothConnect.mServer_InputStream != null) {
                        // 建立一個256位元組的緩衝
                        byte[] buffer = new byte[256];
                        // 每次讀取256位元組,並儲存其讀取的角標
                        int count = BluetoothConnect.mServer_InputStream.read(buffer);

                        if (count > 0) {
                            String value = new String(buffer, 0, count, "utf-8");

                            if (value.equals("accept")) {
                                readerStop = true;
                                Intent it = new Intent(MatchActivity.this, GameActivity.class);
                                startActivity(it);
                            } else if (value.equals("refuse")) {
                                restartApp();
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    });


    // 接收客戶端資料
    private final Thread ReceiveClientThread = new Thread(new Runnable() {
        @Override
        public void run() {
            readerStop = false;

            while (!readerStop) {
                try {
                    if (BluetoothConnect.mClient_InputStream != null) {
                        // 建立一個256位元組的緩衝
                        byte[] buffer = new byte[256];
                        // 每次讀取256位元組,並儲存其讀取的角標
                        int count = BluetoothConnect.mClient_InputStream.read(buffer);

                        if (count > 0) {
                            String value = new String(buffer, 0, count, "utf-8");

                            if (value.equals("duel")) {
                                Looper.prepare();
                                new AlertDialog.Builder(MatchActivity.this)
                                        .setTitle("猜拳")
                                        .setCancelable(false)
                                        .setMessage("接受猜拳對決?")
                                        .setPositiveButton("接受", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                send("accept", 2);
                                                readerStop = true;
                                                Intent it = new Intent(MatchActivity.this, GameActivity.class);
                                                startActivity(it);
                                            }
                                        })
                                        .setNeutralButton("拒絕", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                send("refuse", 2);
                                                restartApp();
                                            }
                                        })
                                        .show();
                                Looper.loop();
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    public static void send(String str, int select) {
        try {
            switch (select) {
                case 1: // 向服務端傳資料
                    BluetoothSocket Server_Socket = BluetoothConnect.mServer_Socket;
                    if (Server_Socket == null) return;
                    if (!Server_Socket.isConnected()) return;
                    OutputStream Server_OutputStream = Server_Socket.getOutputStream();
                    if (Server_OutputStream == null) return;

                    try {
                        Server_OutputStream.write(str.getBytes());
                        Server_OutputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 2: // 向客戶端傳資料
                    BluetoothSocket Client_Socket = BluetoothConnect.mClient_Socket;
                    if (Client_Socket == null) return;
                    if (!Client_Socket.isConnected()) return;
                    OutputStream Client_OutputStream = Client_Socket.getOutputStream();
                    if (Client_OutputStream == null) return;

                    try {
                        Client_OutputStream.write(str.getBytes());
                        Client_OutputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothViewAdapter = new BluetoothViewAdapter(bluetoothAdapter);
        bluetoothViewAdapter.setDelegate(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(bluetoothViewAdapter);

        if (bluetoothAdapter == null) {
            new AlertDialog.Builder(this)
                    .setTitle("本裝置不支援藍芽功能")
                    .setCancelable(false)
                    .setMessage("本裝置不支援藍芽功能，程式即將結束。")
                    .setNeutralButton("結束", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .show();
        }

        Button btnRefresh = findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothViewAdapter.notifyDataSetChanged();
            }
        });

        mBluetoothConnect = new BluetoothConnect(bluetoothAdapter);
        mBluetoothConnect.startAcceptThread();

        ReceiveClientThread.start();

        updateList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateList();
    }

    private void updateList() {
        bluetoothViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void BluetoothBtnConfirmOnClick(View view, BluetoothDevice device) {
        new AlertDialog.Builder(this)
                .setTitle("猜拳")
                .setCancelable(true)
                .setMessage("發起猜拳對決?")
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mBluetoothConnect.Connect(device.getAddress()); // 與服務端連線
                        if (ReceiveServerThread.getState().equals(Thread.State.TERMINATED)) {
                            ReceiveServerThread.interrupt();
                        }
                        ReceiveServerThread.start();
                        send("duel", 1);
                    }
                })
                .setNeutralButton("取消", (dialogInterface, i) -> {
                })
                .show();
    }
}