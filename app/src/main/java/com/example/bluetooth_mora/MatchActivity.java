package com.example.bluetooth_mora;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

public class MatchActivity extends AppCompatActivity implements RecyclerViewAdapter.ViewHolder.RecyclerDelegate {
    private RecyclerViewAdapter recyclerViewAdapter;
    // 獲取到藍芽介面卡
    private BluetoothAdapter bluetoothAdapter;
    // 選中傳送資料的藍芽裝置,全域性變數,否則連線在方法執行完就結束了

    public BluetoothConnect mBluetoothConnect;

    private boolean readerStop;

    public static HashMap<String, String> str2map(String str) {
        str = str.substring(1, str.length() - 1);
        String[] keyValuePairs = str.split(",");
        HashMap<String, String> map = new HashMap<>();

        for (String pair : keyValuePairs) {
            String[] entry = pair.split("=");
            map.put(entry[0].trim(), entry[1].trim());
        }

        return map;
    }

    private final Thread ClientThread = new Thread(new Runnable() {
        @Override
        public void run() {
            Log.d("ClientThread", "run");

            readerStop = false;

            while (!readerStop) {
                if (BluetoothConnect.mServer_InputStream != null) {
                    try {
                        // 建立一個256位元組的緩衝
                        byte[] buffer = new byte[256];
                        // 每次讀取256位元組,並儲存其讀取的角標
                        int count = BluetoothConnect.mServer_InputStream.read(buffer);

                        if (count > 0) {
                            String value = new String(buffer, 0, count, "utf-8");

                            HashMap<String, String> map = str2map(value);

                            if (map.containsKey("challenge")) {
                                readerStop = true;
                                Intent it = new Intent(MatchActivity.this, GameActivity.class);
                                startActivity(it);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    });

    private final Thread ServerThread = new Thread(new Runnable() {
        @Override
        public void run() {
            Log.d("ServerThread", "run");

            readerStop = false;

            while (!readerStop) {
                if (BluetoothConnect.mClient_InputStream != null) {
                    try {
                        // 建立一個256位元組的緩衝
                        byte[] buffer = new byte[256];
                        // 每次讀取256位元組,並儲存其讀取的角標
                        int count = BluetoothConnect.mClient_InputStream.read(buffer);

                        if (count > 0) {
                            String value = new String(buffer, 0, count, "utf-8");

                            HashMap<String, String> map = str2map(value);

                            if (map.containsKey("challenge")) {
                                readerStop = true;
                                map = new HashMap<>();
                                map.put("challenge", "TRUE");
                                send(map, 2);
                                Intent it = new Intent(MatchActivity.this, GameActivity.class);
                                startActivity(it);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    });

    public static void send(HashMap<String, String> dict, int select) {
        try {
            switch (select) {
                case 1:
                    BluetoothSocket Server_Socket = BluetoothConnect.mServer_Socket;
                    if (Server_Socket == null) return;
                    if (!Server_Socket.isConnected()) return;
                    OutputStream mmServer_OutputStream = Server_Socket.getOutputStream();
                    if (mmServer_OutputStream == null) return;

                    try {
                        mmServer_OutputStream.write(dict.toString().getBytes());
                        mmServer_OutputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    BluetoothSocket Client_Socket = BluetoothConnect.mClient_Socket;
                    if (Client_Socket == null) return;
                    if (!Client_Socket.isConnected()) return;
                    OutputStream mmClient_OutputStream = Client_Socket.getOutputStream();
                    if (mmClient_OutputStream == null) return;

                    try {
                        mmClient_OutputStream.write(dict.toString().getBytes());
                        mmClient_OutputStream.flush();
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
        recyclerViewAdapter = new RecyclerViewAdapter(bluetoothAdapter);
        recyclerViewAdapter.setDelegate(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recyclerViewAdapter);

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
                recyclerViewAdapter.notifyDataSetChanged();
            }
        });

        mBluetoothConnect = new BluetoothConnect(bluetoothAdapter);
        mBluetoothConnect.startAcceptThread();

        ServerThread.start();

        System.out.println("onCreate");
        updateList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateList();
    }

    private void updateList() {
        recyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void RecyclerBtnConfirmOnClick(View view, BluetoothDevice device) {
        new AlertDialog.Builder(this)
                .setTitle("猜拳")
                .setCancelable(true)
                .setMessage("發起猜拳對決?")
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mBluetoothConnect.Connect(device.getAddress());
                        if (ClientThread.getState().equals(Thread.State.TERMINATED)) {
                            try {
                                ClientThread.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            ClientThread.start();
                        }
                        HashMap<String, String> dict = new HashMap<>();
                        dict.put("challenge", "TRUE");
                        send(dict, 1);
                    }
                })
                .setNeutralButton("取消", (dialogInterface, i) -> {})
                .show();
    }
}