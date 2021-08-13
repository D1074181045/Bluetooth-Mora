package com.example.bluetooth_mora;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;

public class GameActivity extends AppCompatActivity {
    public InputStream mInputStream;
    public OutputStream mOutputStream;

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

    public Thread Thread = new Thread(new Runnable() {
        @Override
        public void run() {
            readerStop = false;

            while (!readerStop) {
                try {
                    if (mInputStream == null)
                        continue;
                    if (mInputStream.available() <= 0)
                        continue;

                    BufferedReader bufferedInputStream = new BufferedReader(new InputStreamReader(mInputStream));
                    String value = bufferedInputStream.readLine();

                    if (value.length() > 0) {
                        HashMap<String, String> map = str2map(value);

                        if (map.containsKey("mora") )
                            while (SelfMoraSelect.equals("")) ; // 等待用戶猜拳
                        else if (map.containsKey("finish"))
                            mInputStream.close();
                        else
                            continue;

                        Message msg = new Message();
                        msg.obj = map;
                        handler.sendMessage(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }

            }
        }
    });

    public void send(HashMap<String, String> map) {
        if (mOutputStream == null) return;

        PrintWriter printWriter = new PrintWriter(mOutputStream);
        printWriter.write(map.toString() + "\n");
        printWriter.flush();
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            HashMap<String, String> map = (HashMap<String, String>) msg.obj;
            if (map.containsKey("mora"))
                mora(map.get("mora"));
            if (map.containsKey("finish"))
                close_this();
        }
    };

    public void mora(String opposite) {
        switch (SelfMoraSelect) {
            case "Scissors":
                switch (opposite) {
                    case "Scissors":
                        mImgViewOpp.setImageResource(R.drawable.scissors);
                        mTxtResult.setText(getString(R.string.draw));
                        break;
                    case "Stone":
                        mImgViewOpp.setImageResource(R.drawable.stone);
                        mTxtResult.setText(getString(R.string.win));
                        break;
                    case "Paper":
                        mImgViewOpp.setImageResource(R.drawable.paper);
                        mTxtResult.setText(getString(R.string.lose));
                        break;
                }
                break;
            case "Stone":
                switch (opposite) {
                    case "Scissors":
                        mImgViewOpp.setImageResource(R.drawable.scissors);
                        mTxtResult.setText(getString(R.string.lose));
                        break;
                    case "Stone":
                        mImgViewOpp.setImageResource(R.drawable.stone);
                        mTxtResult.setText(getString(R.string.draw));
                        break;
                    case "Paper":
                        mImgViewOpp.setImageResource(R.drawable.paper);
                        mTxtResult.setText(getString(R.string.win));
                        break;
                }
                break;
            case "Paper":
                switch (opposite) {
                    case "Scissors":
                        mImgViewOpp.setImageResource(R.drawable.scissors);
                        mTxtResult.setText(getString(R.string.win));
                        break;
                    case "Stone":
                        mImgViewOpp.setImageResource(R.drawable.stone);
                        mTxtResult.setText(getString(R.string.lose));
                        break;
                    case "Paper":
                        mImgViewOpp.setImageResource(R.drawable.paper);
                        mTxtResult.setText(getString(R.string.draw));
                        break;
                }
                break;
        }
        SelfMoraSelect = "";
        mora_status(true);
    }

    private TextView mTxtResult;
    private ImageView mImgViewOpp, mImgViewSelf;
    private ImageButton mImgBtnScissors, mImgBtnStone, mImgBtnPaper;
    private String SelfMoraSelect = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        setTitle("遊戲頁面");

        if (BluetoothConnect.mServer_Socket != null) {
            try {
                mInputStream = BluetoothConnect.mServer_Socket.getInputStream();
                mOutputStream = BluetoothConnect.mServer_Socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Thread.start();
        } else if (BluetoothConnect.mClient_Socket != null) {
            try {
                mInputStream = BluetoothConnect.mClient_Socket.getInputStream();
                mOutputStream = BluetoothConnect.mClient_Socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Thread.start();
        }

        mImgViewOpp = findViewById(R.id.imgViewOpp);
        mImgViewOpp.setImageResource(R.drawable.question);

        mImgViewSelf = findViewById(R.id.imgViewSelf);
        mImgViewSelf.setImageResource(R.drawable.question);

        mTxtResult = findViewById(R.id.txtResult);
        mImgBtnScissors = findViewById(R.id.imgBtnScissors);
        mImgBtnStone = findViewById(R.id.imgBtnStone);
        mImgBtnPaper = findViewById(R.id.imgBtnPaper);

        // 設定剪刀、石頭、布三個按鈕的Click Listener
        mImgBtnScissors.setOnClickListener(imgBtnScissorsOnClick);
        mImgBtnStone.setOnClickListener(imgBtnStoneOnClick);
        mImgBtnPaper.setOnClickListener(imgBtnPaperOnClick);

        Button btnCloseGame = findViewById(R.id.btnCloseGame);
        btnCloseGame.setOnClickListener(btnCloseGameOnClick);
    }

    private void mora_status(boolean status) {
        mImgBtnScissors.setEnabled(status);
        mImgBtnStone.setEnabled(status);
        mImgBtnPaper.setEnabled(status);
    }

    private final View.OnClickListener imgBtnScissorsOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mImgViewSelf.setImageResource(R.drawable.scissors);
            mImgViewOpp.setImageResource(R.drawable.question);
            mora_status(false);
            HashMap<String, String> map = new HashMap<>();
            SelfMoraSelect = "Scissors";
            map.put("mora", SelfMoraSelect);
            send(map);
        }
    };

    private final View.OnClickListener imgBtnStoneOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mImgViewSelf.setImageResource(R.drawable.stone);
            mImgViewOpp.setImageResource(R.drawable.question);
            mora_status(false);
            HashMap<String, String> map = new HashMap<>();
            SelfMoraSelect = "Stone";
            map.put("mora", SelfMoraSelect);
            send(map);
        }
    };

    private final View.OnClickListener imgBtnPaperOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mImgViewSelf.setImageResource(R.drawable.paper);
            mImgViewOpp.setImageResource(R.drawable.question);
            mora_status(false);
            HashMap<String, String> map = new HashMap<>();
            SelfMoraSelect = "Paper";
            map.put("mora", SelfMoraSelect);
            send(map);
        }
    };

    public void close_this() {
        readerStop = true;
        Intent intent = new Intent(GameActivity.this, MatchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private final View.OnClickListener btnCloseGameOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            HashMap<String, String> map = new HashMap<>();
            map.put("finish", "TRUE");
            send(map);
            close_this();
        }
    };
}