package org.altbeacon.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.altbeacon.WorkTracking.MonitoringActivity;
import org.altbeacon.WorkTracking.MonitoringActivity;
import org.altbeacon.WorkTracking.R;



public class ForegroundService extends Service{

    private static final String TAG = ForegroundService.class.getName();
    private static boolean is_service_running = false;
    private Context mContext;
    private PowerManager.WakeLock mWakeLock;
    private static final int NOTIFICATION_ID = 12345678;
    private static final String STARTFOREGROUND_ACTION = "STARTFOREGROUND_ACTION";
    private static final String STOPFOREGROUND_ACTION = "STOPFOREGROUND_ACTION";

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent !=null && intent.getAction() != null) {
            if (!is_service_running && STARTFOREGROUND_ACTION.equals(intent.getAction())) {
                Log.i(TAG, "Received Start Foreground Intent ");
                showNotification();
                is_service_running = true;
                acquireWakeLock();

            } else if (is_service_running && STOPFOREGROUND_ACTION.equals(intent.getAction())) {
                Log.i(TAG, "Received Stop Foreground Intent");
                is_service_running = false;
                stopForeground(true);
                stopSelf();
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        releaseWakeLock();
        super.onDestroy();
    }

    private void showNotification(){

        Intent notificationIntent = new Intent(mContext, MonitoringActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);

        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, "WeCare")
                .setContentTitle("Foreground Service")
                .setContentText("Test")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
        notification.flags = Notification.FLAG_NO_CLEAR;

        // starts this service as foreground
        startForeground(NOTIFICATION_ID, notification);
    }

    public void acquireWakeLock() {
        final PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        releaseWakeLock();
        //Acquire new wake lock
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG+"PARTIAL_WAKE_LOCK");
        mWakeLock.acquire();
    }

    public void releaseWakeLock() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "WeCare";
            String description = "Notification for WeCare alerts";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("WeCare", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            notificationManager.createNotificationChannel(channel);
        }
    }
}