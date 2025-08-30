package com.deltasoft.pharmatracker;

import android.app.Application;

import com.datadog.android.Datadog;
import com.datadog.android.core.configuration.Configuration;
import com.datadog.android.log.Logger;
import com.datadog.android.log.Logs;
import com.datadog.android.log.LogsConfiguration;
import com.datadog.android.privacy.TrackingConsent;

public class MyApp extends Application {

    private static Logger logger;

    @Override
    public void onCreate() {
        super.onCreate();

        if(true){
        //if (!BuildConfig.DEBUG) {
            // Core Datadog config
            Configuration config = new Configuration.Builder(
                    "pub7c2b17eec3a1e5ef9a3f5ee3d3808803",
                    BuildConfig.DD_APP_ID
            ).build();

            Datadog.initialize(this, config, TrackingConsent.GRANTED);

            // Logs
            LogsConfiguration logsConfig = new LogsConfiguration.Builder().build();
            Logs.enable(logsConfig);

            logger = new Logger.Builder()
                    .setService(BuildConfig.DD_APP_ID)
                    .setNetworkInfoEnabled(true)
                    .build();

            logger.i("✅ Datadog initialized with Logs + RUM + Session Replay");
        }
    }

    public static Logger getLogger() {
        return logger;
    }
}
