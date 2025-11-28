package com.deltasoft.pharmatracker;

import android.app.Application;

import com.datadog.android.Datadog;
import com.datadog.android.core.configuration.Configuration;
import com.datadog.android.log.Logger;
import com.datadog.android.log.Logs;
import com.datadog.android.log.LogsConfiguration;
import com.datadog.android.privacy.TrackingConsent;
import com.datadog.android.rum.Rum;
import com.datadog.android.rum.RumConfiguration;
import com.datadog.android.rum.tracking.ActivityViewTrackingStrategy;
import com.deltasoft.pharmatracker.api.RetrofitClient;
import com.deltasoft.pharmatracker.utils.AppLifecycleTracker;

public class MyApp extends Application {

    private static Logger logger;

    @Override
    public void onCreate() {
        super.onCreate();

        if (!BuildConfig.DEBUG) {
            setupDataDogLogging();
            setupDataDogSessionRecording();
        }

        registerActivityLifecycleCallbacks(AppLifecycleTracker.INSTANCE);

        RetrofitClient.INSTANCE.initialize(getApplicationContext());
    }

    private void setupDataDogLogging() {
        // Core Datadog config
        Configuration config = new Configuration.Builder(
                "pub7c2b17eec3a1e5ef9a3f5ee3d3808803",
                BuildConfig.DD_APP_ID
        ).build();

        Datadog.initialize(this, config, TrackingConsent.GRANTED);
        setUserInfoOnDataDog();

        // Logs
        LogsConfiguration logsConfig = new LogsConfiguration.Builder().build();
        Logs.enable(logsConfig);

        logger = new Logger.Builder()
                .setService(BuildConfig.DD_APP_ID)
                .setNetworkInfoEnabled(true)
                .build();

        logger.i("✅ Datadog initialized with Logging");
    }

    private void setupDataDogSessionRecording() {
        Configuration config = new Configuration.Builder("pubd8ae6cb6870f82eeedf8a4f5bb7a97fb",
                BuildConfig.DD_APP_ID
        ).build();

        Datadog.initialize(this, config, TrackingConsent.GRANTED);
        setUserInfoOnDataDog();


        // RUM
        RumConfiguration rumConfig = new RumConfiguration.Builder("f90cb94e-0022-4e32-b91c-be44d173217b")
                .trackUserInteractions()
                .useViewTrackingStrategy(new ActivityViewTrackingStrategy(true)).build();
        Rum.enable(rumConfig);
        logToDataDog("✅ Datadog initialized with Session Recording");
    }

    public static void logToDataDog(String msg) {
        if(logger!=null){
            logger.i(msg);
        }
    }

    public static void setUserInfoOnDataDog() {
        Datadog.setUserInfo("dummyuser","dummyemail");
    }

}
