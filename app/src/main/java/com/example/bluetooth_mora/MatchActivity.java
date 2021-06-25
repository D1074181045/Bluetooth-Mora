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
import java.io.OutputStream;

public class MatchActivity extends AppCompatActivity implements BluetoothViewAdapter.ViewHolder.BluetoothDelegate, BluetoothConnect.ReceivedDelegate {
    // 藍芽連線
    private BluetoothConnect mBluetoothConnect;

    private BluetoothViewAdapter mBluetoothViewAdapter;

    public void restartApp() {
        Intent intent = getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

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

        setTitle("選擇配對裝置");

        // 獲取到藍芽介面卡
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
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
        mBluetoothViewAdapter = new BluetoothViewAdapter(bluetoothAdapter);
        mBluetoothViewAdapter.setDelegate(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mBluetoothViewAdapter);

        Button btnRefresh = findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBluetoothViewAdapter.notifyDataSetChanged();
            }
        });

        mBluetoothConnect = new BluetoothConnect(bluetoothAdapter);
        mBluetoothConnect.setDelegate(this);
        mBluetoothConnect.startAcceptThread();

        mBluetoothConnect.ReceiveClientThread.start();

        updateList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateList();
    }

    private void updateList() {
        mBluetoothViewAdapter.notifyDataSetChanged();
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
                        if (mBluetoothConnect.ReceiveServerThread.getState().equals(Thread.State.TERMINATED)) {
                            mBluetoothConnect.ReceiveServerThread.interrupt();
                        }
                        mBluetoothConnect.ReceiveServerThread.start();
                        BluetoothViewAdapter.ViewHolder.setBtnDisable(true);
                        updateList();
                        send("duel", 1);
                    }
                })
                .setNeutralButton("取消", (dialogInterface, i) -> {
                })
                .show();
    }

    @Override
    public void ReceivedServer(String value) {
        if (value.equals("accept")) {
            mBluetoothConnect.StopReader();
            Intent it = new Intent(MatchActivity.this, GameActivity.class);
            startActivity(it);
        } else if (value.equals("refuse")) {
            restartApp();
        }
    }

    @Override
    public void ReceivedClient(String value) {
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
                            mBluetoothConnect.StopReader();
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