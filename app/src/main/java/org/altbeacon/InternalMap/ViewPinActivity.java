package org.altbeacon.InternalMap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.altbeacon.WorkTracking.MainApplication;
import org.altbeacon.WorkTracking.R;

import java.util.ArrayList;

public class ViewPinActivity extends AppCompatActivity implements ViewPinRecyclerAdapter.OnCardClickListener {

    String TAG = "ViewPinActivity";
    ViewPinRecyclerAdapter mAdapter;
    private ArrayList<EmployeeCard> mEmployeeArray = new ArrayList<EmployeeCard>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pin);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent startingIntent = getIntent();
        String location = startingIntent.getExtras().getString("Location");
        getSupportActionBar().setTitle("Viewing beacon at " + location);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        MainApplication application = ((MainApplication) getApplicationContext());
        // Inflate the layout for this fragment with the ProductGrid theme

        // Set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ViewPinRecyclerAdapter(
                 this, this);
        recyclerView.setAdapter(mAdapter);


    }

    public void updateEmployeeList(){
        MainApplication application = ((MainApplication) getApplicationContext());
        //Log.i(TAG, "Attempting adapter update");
        if (!application.returnBeaconList().isEmpty()) {
            //Log.i(TAG, "Successful adapter update");
            Log.i(TAG, "The size is: " + application.returnBeaconList().size());
           // mBeaconArray = mAdapter.update(application.returnBeaconList());
        }
    }


    @Override
    public void onCardClick(int position) {
        Log.d("EmployeeCard", "Employee On Card Clicked");
    }

}
