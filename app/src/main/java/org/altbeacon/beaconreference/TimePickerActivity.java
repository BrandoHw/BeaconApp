package org.altbeacon.beaconreference;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Calendar;

public class TimePickerActivity extends AppCompatActivity {

    TimePicker timePicker1, timePicker2;
    int mHours, mMinutes, eHours, eMinutes;
    Toolbar toolbar;
    private Button cancelButton,okButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_picker);

        //Toolbars
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Cancel Button
        cancelButton = findViewById(R.id.appCompatButton2);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                finish();
            }
        });

        //Setup time pickers
        timePicker1 = findViewById(R.id.timePicker1);
        timePicker2 = findViewById(R.id.timePicker2);

        final SharedPreferences sp = this.getSharedPreferences("myShift", 0);
        mHours = sp.getInt("mHours", 9);
        mMinutes = sp.getInt("mMinutes", 0);
        timePicker1.setHour(mHours);
        timePicker1.setMinute(mMinutes);
        eHours = sp.getInt( "eHours", 18);
        eMinutes = sp.getInt("eMinutes", 0);
        timePicker2.setHour(eHours);
        timePicker2.setMinute(eMinutes);

        //Ok Button
        okButton = findViewById(R.id.appCompatButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                int hour1 = timePicker1.getHour();
                int minute1 = timePicker1.getMinute();
                int hour2 = timePicker2.getHour();
                int minute2 = timePicker2.getMinute();
                SharedPreferences.Editor editor = sp.edit();
                editor.putInt("mHours", hour1);
                editor.putInt("mMinutes", minute1);
                editor.putInt("eHours", hour2);
                editor.putInt("eMinutes", minute2);
                editor.commit();
                Log.i("Alarmo", "Main" + hour1 + " " + minute1 + " " + hour2 + " " + minute2);
                finish();
            }
        });
    }
}
