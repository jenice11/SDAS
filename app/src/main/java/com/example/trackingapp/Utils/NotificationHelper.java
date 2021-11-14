package com.example.trackingapp.Utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.trackingapp.R;
import com.google.firebase.messaging.RemoteMessage;

public class NotificationHelper extends ContextWrapper {
    private static final String TRACKING_ID = "com.example.trackingapp";
    private  static final String TRACKING_NAME = "Tracking";

    private NotificationManager manager;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public NotificationHelper(Context base) {
        super(base);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O);
        createChannel();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel edmtchannel = new NotificationChannel(TRACKING_ID,
                TRACKING_NAME,NotificationManager.IMPORTANCE_DEFAULT);
        edmtchannel.enableLights(false);
        edmtchannel.enableVibration(true);
        edmtchannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(edmtchannel);
    }

    public NotificationManager getManager() {
        if (manager == null)
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            return manager;

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getRealTrackingNotification(String title, String content, Uri defaultSound) {
        return new Notification.Builder(getApplicationContext(),TRACKING_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(content)
                .setSound(defaultSound)
                .setAutoCancel(false);

    }
}
