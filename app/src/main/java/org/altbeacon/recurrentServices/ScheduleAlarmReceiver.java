package org.altbeacon.recurrentServices;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import org.altbeacon.WorkTracking.MainApplication;
import org.threeten.bp.LocalTime;

import java.util.Calendar;

public class ScheduleAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Log.i("Alarm", "Starting Schedule Alarm");
        SharedPreferences sp = MainApplication.getContext().getSharedPreferences("myShift", 0);
        int start_hours = sp.getInt("mHours", 9);
        int start_minutes = sp.getInt("mMinutes", 0);
        int end_hours = sp.getInt("eHours", 18);
        int end_minutes = sp.getInt("eMinutes", 0);

        LocalTime currentTime = LocalTime.now();
        LocalTime startTime = LocalTime.of(start_hours, start_minutes);
        LocalTime endTime = LocalTime.of(end_hours, end_minutes);

        Calendar time = Calendar.getInstance();
        MainApplication app = MainApplication.getContext();
        if (currentTime.isBefore(endTime) ){
            time.set(Calendar.HOUR_OF_DAY, end_hours);
            time.set(Calendar.MINUTE, end_minutes);
            time.set(Calendar.SECOND, 0);
            Log.i("Alarm", time.toString());
            alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(time.getTimeInMillis()
                    ,pendingIntent), pendingIntent);
            //app.startAlarm(app.SECONDS);
        } else{
            Calendar tomorrow = Calendar.getInstance();
            tomorrow.add(Calendar.DATE, 1);
            tomorrow.set(Calendar.HOUR_OF_DAY, start_hours);
            tomorrow.set(Calendar.MINUTE, start_minutes);
            tomorrow.set(Calendar.SECOND, 0);
            Log.i("Alarm", tomorrow.toString());
            alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(tomorrow.getTimeInMillis()
                    ,pendingIntent), pendingIntent);
           //app.cancelAlarm();
        }
    }
}