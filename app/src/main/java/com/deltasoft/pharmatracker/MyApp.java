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
import com.datadog.android.sessionreplay.SessionReplay;
import com.datadog.android.sessionreplay.SessionReplayConfiguration;

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
                    "dummyenv"
            ).build();

            Datadog.initialize(this, config, TrackingConsent.GRANTED);

            // Logs
            LogsConfiguration logsConfig = new LogsConfiguration.Builder().build();
            Logs.enable(logsConfig);

            logger = new Logger.Builder()
                    .setService("pharmatracker-android")
                    .setNetworkInfoEnabled(true)
                    .build();

            // RUM
            RumConfiguration rumConfig = new RumConfiguration.Builder(BuildConfig.DD_APP_ID)
                    .useViewTrackingStrategy(new ActivityViewTrackingStrategy(true)).build();
            Rum.enable(rumConfig);

            // Session Replay
            SessionReplayConfiguration srConfig =
                    new SessionReplayConfiguration.Builder().build();
            SessionReplay.enable(srConfig);

            logger.i("✅ Datadog initialized with Logs + RUM + Session Replay");
        }
    }

    public static Logger getLogger() {
        return logger;
    }
}
