package org.altbeacon.beaconreference;

import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.network.LocationTimeStamp;
import org.altbeacon.beaconreference.BeaconReferenceApplication;

import java.util.ArrayList;


public class LocationTimestampFragment extends Fragment {
    public static final String ARG_PAGE = "ARG_PAGE";
    private static final String TAG = "LocationTimeStampFrag";
    LocationRecyclerViewAdapter adapter;
    //BeaconReferenceApplication application;

    TextView currentLocationId;
    TextView currentTimestampId;

    public static LocationTimestampFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        LocationTimestampFragment fragment = new LocationTimestampFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView running");
        BeaconReferenceApplication application = ((BeaconReferenceApplication) getActivity().getApplicationContext());
        // Inflate the layout for this fragment with the ProductGrid theme
        View view = inflater.inflate(R.layout.location_timestamp_fragment, container, false);

        //Set up Current Location Text View
        currentLocationId = (TextView) view.findViewById(R.id.currentLocation);
        currentTimestampId = (TextView) view.findViewById(R.id.currentTimestamp);
        String location = application.lastLocation();
        long timestamp = application.latestTimeStamp();
        String timestampString;
        if (location == null)
            location = "No Current Location";
        if (timestamp == 1)
            timestampString = " ";
        else
            timestampString = application.getDateCurrentTimeZone(timestamp);
        updateCurrentLocation(application.lastLocation(),timestampString);

        // Set up the RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LocationRecyclerViewAdapter(
                application.locationList());
        recyclerView.setAdapter(adapter);


        return view;
    }


    public void updateAdapter(){
        BeaconReferenceApplication application = ((BeaconReferenceApplication) getActivity().getApplicationContext());
        Log.i(TAG, "Attempting adapter update");
        if (application.locationList() != null) {
            Log.i(TAG, "Successful adapter update");
            adapter.update(application.locationList());
        }
    }

    public void updateCurrentLocation(String currentLocation, String timeStamp){
        currentLocationId.setText("Current Location: " + currentLocation);
        currentTimestampId.setText("[" + timeStamp + "]");
    }
}

