package org.altbeacon.WorkTracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;


public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Alarm Manager is working");
        LocalTime now = LocalTime.now(ZoneId.systemDefault());
        MainApplication application = ((MainApplication) context.getApplicationContext());
        if (application.isBetweenWorkHours(now)) {
            Log.i(TAG, "Monitoring Enabled");
            //Initialize the application
            application.enableMonitoring();
        } else {
            application.disableMonitoring();
            long latestTime = application.latestTimeStamp();
            long currentTime = System.currentTimeMillis();
            long duration = currentTime - latestTime;
            String lastLocation = application.lastLocation();
            String displayCurrentTime = application.getDateCurrentTimeZone(currentTime);
            application.appendToList(lastLocation, currentTime, duration);
            application.updateFragment();
            application.setClosestLocationEnd();
            application.updateCurrentLocationLTS("END", displayCurrentTime);
            application.updateCurrentLocationMaps("END", displayCurrentTime);
            Log.i(TAG, Long.toString(latestTime));
            Log.i(TAG, "Monitoring Disabled");
            application.setAlarm(true);
        }
    }
}
