package com.lawlett.taskmanageruikit.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.main.MainActivity;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.lawlett.taskmanageruikit.utils.App.CHANNEL_ID_HOURS;

public class MessageService extends BroadcastReceiver {

    public static final String TITLE = "title";
    public static final String TEXT = "text";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra(TITLE);
        String text = intent.getStringExtra(TEXT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            if (notificationManager.getNotificationChannel(CHANNEL_ID_HOURS) == null) {
                notificationManager.createNotificationChannel(
                        new NotificationChannel(
                                CHANNEL_ID_HOURS,
                                "Whatever",
                                NotificationManager.IMPORTANCE_HIGH
                        )
                );
            }
            Intent intent1 = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 2
                    , intent1, PendingIntent.FLAG_ONE_SHOT);
            Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID_HOURS)
                    .setSmallIcon(R.drawable.ic_planner)
                    .setContentTitle(title).setContentText(text)
                    .setOnlyAlertOnce(true)
                    .setContentIntent(pendingIntent)
                    .build();
            notificationManager.notify(2, notification);
        } else {
            Intent intent1 = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 2, intent1, PendingIntent.FLAG_ONE_SHOT);
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_planner)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setAutoCancel(true)
                    .setSound(soundUri)
                    .setContentIntent(pendingIntent);
            Notification notification = builder.build();
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(2, notification);
        }
    }
}

