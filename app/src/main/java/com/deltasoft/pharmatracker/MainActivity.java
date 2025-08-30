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
        tv.setText(String.format("Hello from Java!!\nAPI = %s", BuildConfig.BASE_API_URL));

        MyApp.logToDataDog("MainActivity has started");
        MyApp.logToDataDog("The API Base URL is: "+BuildConfig.BASE_API_URL);

    }


}
