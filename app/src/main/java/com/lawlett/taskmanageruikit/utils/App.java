package com.lawlett.taskmanageruikit.utils;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.widget.Toast;

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
    private static Resources resources;

    @Override
    public void onCreate() {
        super.onCreate();
        dataBase = Room.databaseBuilder(this, AppDataBase.class, "database")
             .fallbackToDestructiveMigration().allowMainThreadQueries().build();

        createNotificationChannel();
        resources = getResources();

    }
    public static AppDataBase getDataBase() {
        return dataBase;
    }

    public static void showToast(Context context,String text){
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
    public static Resources getAppResources() {
        return resources;
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
