package com.deltasoft.pharmatracker;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ✅ Show BuildConfig value
        TextView tv = findViewById(R.id.helloText);
        tv.setText("Hello from Java!!\nAPI = " + BuildConfig.BASE_API_URL);
    }
}
