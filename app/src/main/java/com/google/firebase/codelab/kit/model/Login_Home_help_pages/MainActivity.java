package com.google.firebase.codelab.kit.model.Login_Home_help_pages;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.codelab.kit.model.R;

public class MainActivity extends AppCompatActivity {

    ImageButton logostartBut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logostartBut = (ImageButton) findViewById(R.id.logostartBut);

        logostartBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent start = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(start);
            }
        });


    }
}
