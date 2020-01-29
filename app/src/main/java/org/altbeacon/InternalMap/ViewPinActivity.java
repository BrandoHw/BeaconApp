package org.altbeacon.InternalMap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beaconreference.BeaconReferenceApplication;
import org.altbeacon.beaconreference.BeaconSettingsRecycleViewAdapter;
import org.altbeacon.beaconreference.R;

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


        BeaconReferenceApplication application = ((BeaconReferenceApplication) getApplicationContext());
        // Inflate the layout for this fragment with the ProductGrid theme

        // Set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ViewPinRecyclerAdapter(
                 this, this);
        recyclerView.setAdapter(mAdapter);


    }

    @Override
    public void onCardClick(int position) {
        Log.d("EmployeeCard", "Employee On Card Clicked");
    }

}
