package com.example.bluetooth_mora;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class GameActivity2 extends AppCompatActivity {
    public void mora(int opposite) {
        switch (SelfMoraSelect) {
            case "Scissors":
                switch (opposite) {
                    case 1:
                        mImgViewOpp.setImageResource(R.drawable.scissors);
                        mTxtResult.setText(getString(R.string.draw));
                        break;
                    case 2:
                        mImgViewOpp.setImageResource(R.drawable.stone);
                        mTxtResult.setText(getString(R.string.win));
                        break;
                    case 3:
                        mImgViewOpp.setImageResource(R.drawable.paper);
                        mTxtResult.setText(getString(R.string.lose));
                        break;
                }
                break;
            case "Stone":
                switch (opposite) {
                    case 1:
                        mImgViewOpp.setImageResource(R.drawable.scissors);
                        mTxtResult.setText(getString(R.string.lose));
                        break;
                    case 2:
                        mImgViewOpp.setImageResource(R.drawable.stone);
                        mTxtResult.setText(getString(R.string.draw));
                        break;
                    case 3:
                        mImgViewOpp.setImageResource(R.drawable.paper);
                        mTxtResult.setText(getString(R.string.win));
                        break;
                }
                break;
            case "Paper":
                switch (opposite) {
                    case 1:
                        mImgViewOpp.setImageResource(R.drawable.scissors);
                        mTxtResult.setText(getString(R.string.win));
                        break;
                    case 2:
                        mImgViewOpp.setImageResource(R.drawable.stone);
                        mTxtResult.setText(getString(R.string.lose));
                        break;
                    case 3:
                        mImgViewOpp.setImageResource(R.drawable.paper);
                        mTxtResult.setText(getString(R.string.draw));
                        break;
                }
                break;
        }
    }

    private TextView mTxtResult;
    private ImageView mImgViewOpp, mImgViewSelf;
    private String SelfMoraSelect = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        setTitle("遊戲頁面");

        mImgViewOpp = findViewById(R.id.imgViewOpp);
        mImgViewSelf = findViewById(R.id.imgViewSelf);
        mTxtResult = findViewById(R.id.txtResult);

        ImageButton mImgBtnScissors = findViewById(R.id.imgBtnScissors);
        ImageButton mImgBtnStone = findViewById(R.id.imgBtnStone);
        ImageButton mImgBtnPaper = findViewById(R.id.imgBtnPaper);

        // 設定剪刀、石頭、布三個按鈕的Click Listener
        mImgBtnScissors.setOnClickListener(imgBtnScissorsOnClick);
        mImgBtnStone.setOnClickListener(imgBtnStoneOnClick);
        mImgBtnPaper.setOnClickListener(imgBtnPaperOnClick);

        Button btnCloseGame = findViewById(R.id.btnCloseGame);
        btnCloseGame.setOnClickListener(btnCloseGameOnClick);
    }

    private final View.OnClickListener imgBtnScissorsOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mImgViewSelf.setImageResource(R.drawable.scissors);
            SelfMoraSelect = "Scissors";
            int iComPlay = (int)(Math.random()*3 + 1);
            mora(iComPlay);
        }
    };

    private final View.OnClickListener imgBtnStoneOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mImgViewSelf.setImageResource(R.drawable.stone);
            SelfMoraSelect = "Stone";
            int iComPlay = (int)(Math.random()*3 + 1);
            mora(iComPlay);
        }
    };

    private final View.OnClickListener imgBtnPaperOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mImgViewSelf.setImageResource(R.drawable.paper);
            SelfMoraSelect = "Paper";
            int iComPlay = (int)(Math.random()*3 + 1);
            mora(iComPlay);
        }
    };

    private final View.OnClickListener btnCloseGameOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            finish();
        }
    };
}