package org.altbeacon.WorkTracking;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.applandeo.materialcalendarview.CalendarView;

import org.altbeacon.objects.MyEventDay;

public class AddNoteActivity extends AppCompatActivity {

    private CalendarView datePicker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        datePicker = (CalendarView) findViewById(R.id.datePicker);
        Button button = (Button) findViewById(R.id.addNoteButton);
        final EditText noteEditText = (EditText) findViewById(R.id.noteEditText);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();

                MyEventDay myEventDay = new MyEventDay(datePicker.getSelectedDate(),
                        R.drawable.ic_message_black_48dp, noteEditText.getText().toString());

                returnIntent.putExtra(CalendarActivity.RESULT, myEventDay);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });

        /*
        datePicker.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                Intent returnIntent = new Intent();

                MyEventDay myEventDay = new MyEventDay(datePicker.getSelectedDate(),
                        R.drawable.ic_message_black_48dp, noteEditText.getText().toString());

                returnIntent.putExtra(CalendarActivity.RESULT, myEventDay);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });*/
    }
}
