package com.example.jgreenb2.myselfie;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by jgreenb2 on 4/24/15.
 */
public class SelfieAlarms extends BroadcastReceiver {
    private Context mContext;
    private Intent mRestartSelfie;
    private final int MY_NOTIFICATION_ID = 11151990;

    public SelfieAlarms(Context appContext) {
        mContext = appContext;
        mRestartSelfie = new Intent(mContext,MainActivity.class);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mRestartSelfie,
                                                                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder nbuild = new Notification.Builder(mContext);
        Notification selfieNotify = nbuild.setSmallIcon(android.R.drawable.ic_menu_camera)
                .setTicker("Time for a Selfie!")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(MY_NOTIFICATION_ID,selfieNotify);
    }
}
