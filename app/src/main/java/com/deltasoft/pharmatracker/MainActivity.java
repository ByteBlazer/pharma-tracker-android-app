package com.deltasoft.pharmatracker;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv = findViewById(R.id.helloText);
        tv.setText("Hello from Java!!\nAPI = " + BuildConfig.BASE_API_URL);

        if (!BuildConfig.DEBUG && MyApp.getLogger() != null) {
            MyApp.getLogger().addAttribute("apiUrl", BuildConfig.BASE_API_URL);
            MyApp.getLogger().i("MainActivity started");
        }

        // ✅ No manual RUM calls needed — v2 SDK auto-tracks Activity as a RUM view,
        // and user taps as RUM actions.
    }
}
