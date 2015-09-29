package com.pollytronics.qwax;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CalendarContract.Instances;
import android.util.Log;

public class MyService extends Service {

    private static final String TAG = "MyService";

    private static int cnt = 0;
    private Handler handler;
    private ToasterLoop toasterLoop;
    private static final int NOTIFICATION_ID = 1;
    private Notification.Builder notificationBuilder;

    public MyService() {
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate()");
        toasterLoop = new ToasterLoop();
        handler = new Handler();
        handler.post(toasterLoop);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notificationBuilder = new Notification.Builder(getApplicationContext())
                .setContentTitle("Qwax")
                .setContentText("reading calendar...")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setShowWhen(false);
        startForeground(NOTIFICATION_ID, notificationBuilder.getNotification());
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        handler.removeCallbacks(toasterLoop);
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not implemented");
    }

    private class ToasterLoop implements Runnable {
        @Override
        public void run() {
            updateNotification();
            handler.postDelayed(this, 10000);
        }
    }

    private void updateNotification() {
        long now = System.currentTimeMillis();
        ContentResolver cr = getContentResolver();
        String[] projection = new String[]{Instances.BEGIN, Instances.END, Instances.TITLE};
        Cursor cur = Instances.query(
                cr,
                projection,
                now,
                now + 1000 * 60 * 60 * 12); // looking 12 hours ahead for events.
        String nextEventTitle = null;
        String currentEventTitle = null;
        long nextStartTime = Long.MAX_VALUE;
        long lastStartTime = Long.MIN_VALUE;
        long currentStopTime = 0;
        while (cur.moveToNext()) {
            long startTime = cur.getLong(cur.getColumnIndexOrThrow(Instances.BEGIN));
            long stopTime = cur.getLong(cur.getColumnIndexOrThrow(Instances.END));
            // find first start in future, save corresponding start and title
            if ((startTime > now) && (startTime < nextStartTime)) {
                nextStartTime = startTime;
                nextEventTitle = cur.getString(cur.getColumnIndexOrThrow(Instances.TITLE));
            }
            // find last start in past, save the corresponding title and stop time
            if ((startTime < now) && (startTime > lastStartTime)) {
                lastStartTime = startTime;
                currentStopTime = stopTime;
                currentEventTitle = cur.getString(cur.getColumnIndexOrThrow(Instances.TITLE));
            }
        }
        cur.close();
        long nextStartMinutes = (nextStartTime - now) / 1000 / 60;
        long currentStopMinutes = (currentStopTime - now) / 1000 / 60;
        String nextString, currentString;
        if (nextStartTime < Long.MAX_VALUE) {
            nextString = "next (" + nextStartMinutes + "min): " + nextEventTitle;
        } else {
            nextString = "nothing coming up";
        }
        if (lastStartTime > Long.MIN_VALUE) {
            currentString = "currently (" + currentStopMinutes + "min): " + currentEventTitle;
        } else {
            currentString = "nothing going on";
        }
        Notification notification = notificationBuilder
                .setContentTitle(nextString)
                .setContentText(currentString)
                .getNotification();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
