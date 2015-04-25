package com.example.jgreenb2.myselfie;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

/**
 * Created by jgreenb2 on 4/24/15.
 */
public class SelfieAlarms extends BroadcastReceiver {
    private Context mContext;
    private Intent mRestartSelfie;
    private final int MY_NOTIFICATION_ID = 11151990;
    private final int TWO_MINUTES = 2*60*1000;
    private final int INITIAL_DELAY = 1000;

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

    public void setSelfieAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, SelfieAlarms.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0,intent,0);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME,
                               SystemClock.elapsedRealtime()+INITIAL_DELAY,
                               TWO_MINUTES,
                               pendingIntent);
    }

    public void cancelSelfieAlarm(Context context) {
        Intent intent = new Intent(context, SelfieAlarms.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
    }
}
