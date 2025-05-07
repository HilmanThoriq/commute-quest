package com.example.commutequest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class OnBoardingActivity extends AppCompatActivity {

    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);

        // Initialize views
        startButton = findViewById(R.id.btn_start_adventure);

        // Set up click listener for start button
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the Auth Activity
                Intent intent = new Intent(OnBoardingActivity.this, AuthActivity.class);
                startActivity(intent);

            }
        });
    }
}