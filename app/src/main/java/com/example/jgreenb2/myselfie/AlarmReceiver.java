package com.example.jgreenb2.myselfie;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.SystemClock;

/**
 * Created by jgreenb2 on 4/25/15.
 */
public class AlarmReceiver extends BroadcastReceiver {
    private final int MY_NOTIFICATION_ID = 1;
    private final Uri soundURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    static private AlarmManager mAlarmManager;
    static private Intent mNotificationReceiverIntent;
    static private PendingIntent mNotificationReceiverPendingIntent;

    public boolean areAlarmsEnabled() {
        return mAlarmsEnabled;
    }


    private boolean mAlarmsEnabled=true;

    private static final long SELFIE_INTERVAL = 60 * 60 * 1000L;

    private static Context mActivityContext;

    public AlarmReceiver(Context activityContext) {
        mActivityContext = activityContext;

        // set up the annoying alarm
        // Get the AlarmManager Service
        mAlarmManager = (AlarmManager) activityContext.getSystemService(Context.ALARM_SERVICE);

        // Create an Intent to broadcast to the AlarmReceiver
//        mNotificationReceiverIntent = new Intent(activityContext,
//                AlarmReceiver.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
//                Intent.FLAG_ACTIVITY_NEW_TASK);
        mNotificationReceiverIntent = new Intent(activityContext,
                AlarmReceiver.class).addFlags(0);

        // Create an PendingIntent that holds the NotificationReceiverIntent
        mNotificationReceiverPendingIntent = PendingIntent.getBroadcast(
                activityContext, 0, mNotificationReceiverIntent, 0);

    }

    // we need an empty constructor to receive events
    public AlarmReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent restartSelfie = new Intent(context,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, restartSelfie,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder nbuild = new Notification.Builder(context);
        Notification selfieNotify = nbuild.setSmallIcon(android.R.drawable.ic_menu_camera)
                .setTicker("Time for a Selfie!")
                .setContentTitle("Take a Selfie!")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSound(soundURI)
                .build();

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(MY_NOTIFICATION_ID,selfieNotify);
    }
    public void setSelfieAlarm() {
        // Set repeating alarm
        mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + SELFIE_INTERVAL,
                SELFIE_INTERVAL,
                mNotificationReceiverPendingIntent);
        mAlarmsEnabled=true;
    }

    public void cancelSelfieAlarm() {
        mAlarmManager.cancel(mNotificationReceiverPendingIntent);
        mAlarmsEnabled=false;
    }
}
