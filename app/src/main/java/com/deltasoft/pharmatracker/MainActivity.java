package com.deltasoft.pharmatracker;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.datadog.android.Datadog;
import com.datadog.android.core.configuration.Configuration;
import com.datadog.android.privacy.TrackingConsent;
import com.datadog.android.rum.Rum;
import com.datadog.android.rum.RumConfiguration;
import com.datadog.android.rum.tracking.ActivityViewTrackingStrategy;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv = findViewById(R.id.helloText);
        tv.setText("Hello from Java!!\nAPI = " + BuildConfig.BASE_API_URL);

        if(true){
//        if (!BuildConfig.DEBUG && MyApp.getLogger() != null) {
            MyApp.getLogger().addAttribute("apiUrl", BuildConfig.BASE_API_URL);
            MyApp.getLogger().i("MainActivity started");


        Configuration config = new Configuration.Builder(
                "pubd8ae6cb6870f82eeedf8a4f5bb7a97fb",
                BuildConfig.DD_APP_ID
        ).build();

        Datadog.initialize(this, config, TrackingConsent.GRANTED);

        // RUM
        RumConfiguration rumConfig = new RumConfiguration.Builder("f90cb94e-0022-4e32-b91c-be44d173217b")
                .trackUserInteractions()
                .useViewTrackingStrategy(new ActivityViewTrackingStrategy(true)).build();
        Rum.enable(rumConfig);

        }


    }
}
