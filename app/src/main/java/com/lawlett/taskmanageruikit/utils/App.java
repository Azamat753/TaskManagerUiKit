package com.lawlett.taskmanageruikit.utils;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.multidex.MultiDex;
import androidx.room.Room;

import com.lawlett.taskmanageruikit.room.AppDataBase;

import java.util.ArrayList;
import java.util.List;

public class App extends Application {
    public static App instance;
    private static AppDataBase dataBase;
    public static final String CHANNEL_ID = "exampleChannel";
    public static final String CHANNEL_ID_HOURS = "exampleChannelHours";
//    public static final String CHANNEL_ID_DAY = "exampleChannelDay";

    @Override
    public void onCreate() {
        super.onCreate();
        dataBase = Room.databaseBuilder(this, AppDataBase.class, "database")
             .fallbackToDestructiveMigration().allowMainThreadQueries().build();
//
//        Room.databaseBuilder(this,AppDataBase.class,"database")
//                .addMigrations(MIGRATION_2_3).build();
        createNotificationChannel();

    }
    public static AppDataBase getDataBase() {
        return dataBase;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Example Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationChannel channel2 = new NotificationChannel(
                    CHANNEL_ID_HOURS, "Example Channel2",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            List<NotificationChannel> notificationChannels = new ArrayList<>();
            notificationChannels.add(channel);
            notificationChannels.add(channel2);
            manager.createNotificationChannels(notificationChannels);

        }
    }


}
