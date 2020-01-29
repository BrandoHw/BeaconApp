package org.altbeacon.beaconreference;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewDebug;
import android.widget.TimePicker;

import android.content.SharedPreferences;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

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
        Log.i("Alarmo", mHours + " " + mMinutes + " " + eHours + " " + eMinutes);
    }
}