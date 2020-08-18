package org.altbeacon.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;

import android.provider.Settings.System;
import android.provider.Settings.Secure;
import org.altbeacon.WorkTracking.R;
import org.altbeacon.utils.DateStringUtils;

public final class UserUtils {

    private static final String TAG = "userUtil";


    public static void userEntryInit(Context context, UserRepository userRepository){
        //Get IMEI number
        String androidID = Settings.System.getString(context.getContentResolver(), Secure.ANDROID_ID);
        Log.i(TAG, "IMEI Number:" + androidID);
        SharedPreferences sp = context.getSharedPreferences("myProfile", 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("imei", androidID);
        editor.apply();
        String name = sp.getString("profileName", "Name Unavailable");
        String email = sp.getString("profileEmail", "Email Unavailable");


        UserEntry userEntry = new UserEntry(androidID, name, email,
                context.getResources().getInteger(R.integer.trans_interval),
                DateStringUtils.getDateCurrentTimeZone(java.lang.System.currentTimeMillis()));
    }
}
