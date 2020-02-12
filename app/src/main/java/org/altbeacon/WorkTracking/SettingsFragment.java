package org.altbeacon.WorkTracking;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import org.threeten.bp.LocalTime;

public class SettingsFragment extends PreferenceFragmentCompat{
    public static final String ARG_PAGE = "ARG_PAGE";
    String TAG = "Alarm";
    int mHours;
    int mMinutes;
    int eHours;
    int eMinutes;
    String amPm;
    TimePickerDialog timePickerDialog;
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_fragment, rootKey);

        Preference myPref = findPreference("morning");
        Preference myPref2 = findPreference("evening");
        SharedPreferences sp = getActivity().getSharedPreferences("myShift", 0);
        mHours = sp.getInt("mHours", 9);
        mMinutes = sp.getInt("mMinutes", 0);
        setPreferenceTime(mHours, mMinutes, myPref);
        eHours = sp.getInt("eHours", 18);
        eMinutes = sp.getInt("eMinutes", 0);
        setPreferenceTime(eHours, eMinutes, myPref2);
        setAlarm();

        myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), TimePickerActivity.class);
                startActivity(intent);
                return false;
            }
        });

        myPref2.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), TimePickerActivity.class);
                startActivity(intent);
                return false;
            }
        });

    }

    public static SettingsFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void setPreferenceTime(int Hours, int Minutes, Preference myPref){
        if (Hours == 0) {
            Hours = 12;
            amPm = "AM";
        }
        else if (Hours > 12) {
            Hours -= 12;
            amPm = "PM";
        }
        else if (Hours == 12) {
            amPm = "PM";
        }
        else {
            amPm = "AM";
        }

        if (Minutes > 9)
            myPref.setTitle(Hours + ":" + Minutes + amPm);
        else
            myPref.setTitle(Hours + ":0" + Minutes + amPm);
    }

    @Override
    public void onResume() {
        super.onResume();
        Preference myPref = findPreference("morning");
        Preference myPref2 = findPreference("evening");
        SharedPreferences sp = getActivity().getSharedPreferences("myShift", 0);
        mHours = sp.getInt("mHours", 9);
        mMinutes = sp.getInt("mMinutes", 0);
        setPreferenceTime(mHours, mMinutes, myPref);
        eHours = sp.getInt("eHours", 18);
        eMinutes = sp.getInt("eMinutes", 0);
        setPreferenceTime(eHours, eMinutes, myPref2);
        Log.i("Alarm", mHours + " " + mMinutes + " " + eHours + " " + eMinutes);
        setAlarm();
    }

    public void setAlarm(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        MainApplication application = (MainApplication) getActivity().getApplicationContext();
        if (preferences.getBoolean("alarmEnable", true)){
            if (application.isBetweenWorkHours(LocalTime.now()))
                application.setAlarm(false);
            else
                application.setAlarm(true);
        }
        else
            application.cancelAlarm();
    }
}