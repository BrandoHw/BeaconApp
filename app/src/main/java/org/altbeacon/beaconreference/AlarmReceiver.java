package org.altbeacon.beaconreference;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;

import org.altbeacon.beacon.startup.RegionBootstrap;


public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Alarm Manager is WORKING");
        LocalTime now = LocalTime.now(ZoneId.systemDefault());
        BeaconReferenceApplication application = ((BeaconReferenceApplication) context.getApplicationContext());
        if (application.isBetweenWorkHours(now)) {
            application.enableMonitoring();
        } else {
            application.disableMonitoring();
            long latestTime = application.latestTimeStamp();
            long currentTime = System.currentTimeMillis();
            long duration = currentTime - latestTime;
            String displayTimeSpent = application.getDurationBreakdown(duration);
            String displayCurrentTime = application.getDateCurrentTimeZone(currentTime);
            application.logToDisplay("[" + displayCurrentTime + "] Employee ended their shift at: "
                    + application.lastLocation() + " Duration: " + displayTimeSpent);
        }
    }
}
