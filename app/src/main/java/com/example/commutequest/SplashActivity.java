package com.example.commutequest;

import androidx.appcompat.app.AppCompatActivity;
import com.example.commutequest.util.SessionManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SessionManager sessionManager = new SessionManager(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Cek apakah pengguna sudah login
                if (sessionManager.isLoggedIn()) {
                    // Jika sudah login, langsung ke MainActivity
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    // Jika belum login, pergi ke OnBoardingActivity
                    Intent intent = new Intent(SplashActivity.this, OnBoardingActivity.class);
                    startActivity(intent);
                }
                // Tutup SplashActivity supaya tidak bisa kembali ke sini
                finish();
            }
        }, 3000); // Delay menuju halaman selanjutnya selama 3 detik


    }
}