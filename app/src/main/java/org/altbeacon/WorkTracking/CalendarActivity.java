package org.altbeacon.WorkTracking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.altbeacon.objects.MyEventDay;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {
    public static final String RESULT = "result";
    public static final String EVENT = "event";
    private static final int ADD_NOTE = 44;

    private CalendarView mCalendarView;
    private List<EventDay> mEventDays = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCalendarView = (CalendarView) findViewById(R.id.calendarView);
        loadData();
        mCalendarView.setEvents(mEventDays);

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNote();
            }
        });

        mCalendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                previewNote(eventDay);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_NOTE && resultCode == RESULT_OK) {
            MyEventDay myEventDay = data.getParcelableExtra(RESULT);
            mCalendarView.setDate(myEventDay.getCalendar());
            mEventDays.add(myEventDay);
            mCalendarView.setEvents(mEventDays);
            saveData();
            loadData();
        }
    }

    private void addNote() {
       Intent intent = new Intent(this, AddNoteActivity.class);
       startActivityForResult(intent, ADD_NOTE);
    }

    private void previewNote(EventDay eventDay) {
        //Intent intent = new Intent(this, NotePreviewActivity.class);
       // if(eventDay instanceof MyEventDay){
        //    intent.putExtra(EVENT, (MyEventDay) eventDay);
       // }
      //  startActivity(intent);
    }


    private void saveData(){
        SharedPreferences sp = getSharedPreferences("myEvents", 0);
        SharedPreferences.Editor editor = sp.edit();
        Gson gson = new Gson();
        String json = gson.toJson(mEventDays);
        editor.putString("eventList", json);
        editor.commit();
    }

    private void loadData() {
        SharedPreferences sp = getSharedPreferences("myEvents", 0);
        Gson gson = new Gson();
        String json =  sp.getString( "eventList", null);
        Log.i("GSON", json);
        Type type = new TypeToken<ArrayList<MyEventDay>>() {}.getType();
        mEventDays = gson.fromJson(json, type);

        if (mEventDays == null) {
            mEventDays = new ArrayList<>();
        }

        //Method to check and remove old event days from the list
    }

}