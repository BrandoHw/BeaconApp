package org.altbeacon.recurrentServices;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import org.altbeacon.WorkTracking.MainApplication;
import org.threeten.bp.LocalTime;

import java.util.Calendar;

public class AlarmScheduler {


    public void startScheduleAlarm(){
        AlarmManager alarmManager = (AlarmManager) MainApplication.getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(MainApplication.getContext(), ScheduleAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainApplication.getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
        Log.i("Alarm", time.getTime().toString());

        if (currentTime.isBefore(startTime)){
            Log.i("Alarm", "IsBefore");
            time.set(Calendar.HOUR_OF_DAY, start_hours);
            time.set(Calendar.MINUTE, start_minutes);
            time.set(Calendar.SECOND, 0);
            Log.i("Alarm", time.getTime().toString());
            alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(time.getTimeInMillis()
                    ,pendingIntent), pendingIntent);
        }else if (currentTime.isAfter(startTime) && currentTime.isBefore(endTime)){
            Log.i("Alarm", "IsBetween");
            time.set(Calendar.HOUR_OF_DAY, end_hours);
            time.set(Calendar.MINUTE, end_minutes);
            time.set(Calendar.SECOND, 0);
            Log.i("Alarm", time.getTime().toString());
            //startAlarm(SECONDS);
            alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(time.getTimeInMillis()
                    ,pendingIntent), pendingIntent);
        } else{
            Calendar tomorrow = Calendar.getInstance();
            Log.i("Alarm", "IsAfter");
            tomorrow.add(Calendar.DATE, 1);
            tomorrow.set(Calendar.HOUR_OF_DAY, start_hours);
            tomorrow.set(Calendar.MINUTE, start_minutes);
            tomorrow.set(Calendar.SECOND, 0);
            Log.i("Alarm", tomorrow.getTime().toString());
            alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(tomorrow.getTimeInMillis()
                    ,pendingIntent), pendingIntent);
        }
    }
}
