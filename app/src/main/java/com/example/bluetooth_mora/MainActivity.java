package com.example.bluetooth_mora;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("遊戲模式");

        Button with_computer_pk = findViewById(R.id.with_computer_mora);
        with_computer_pk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(MainActivity.this, GameActivity2.class);
                startActivity(it);
            }
        });

        Button with_player_pk = findViewById(R.id.with_player_mora);
        with_player_pk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(MainActivity.this, MatchActivity.class);
                startActivity(it);
            }
        });
    }
}