package org.altbeacon.beaconreference;

import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.NetworkImageView;

import org.altbeacon.network.LocationTimeStamp;

import java.util.ArrayList;

public class LocationRecyclerViewAdapter extends RecyclerView.Adapter<LocationRecyclerViewAdapter.LocationsViewHolder>{

    private static final String TAG = "LocRecyclerViewAdapter";
    private ArrayList<LocationTimeStamp> mlocationTimeStamps = new ArrayList<>();

    LocationRecyclerViewAdapter(ArrayList<LocationTimeStamp> lts){
        mlocationTimeStamps = lts;
    }
    @NonNull
    @Override
    public LocationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_timestamp_card, parent, false);
        return new LocationsViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationsViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder called");
        if (mlocationTimeStamps != null && position < mlocationTimeStamps.size()){
            LocationTimeStamp lts = mlocationTimeStamps.get(position);
            holder.timestamp.setText("[" + lts.timeStamp + "]");
            holder.location.setText(lts.location);
            holder.duration.setText(lts.duration);
            Log.i("Test Duration", lts.duration);
        }
    }


    @Override
    public int getItemCount() {
        return mlocationTimeStamps.size();
    }

    public class LocationsViewHolder extends RecyclerView.ViewHolder {

        public TextView location;
        public TextView timestamp;
        public TextView duration;


        public LocationsViewHolder(@NonNull View itemView) {
            super(itemView);
            timestamp = itemView.findViewById(R.id.timestamp);
            location = itemView.findViewById(R.id.location);
            duration = itemView.findViewById(R.id.duration);
        }
    }

    public void update(ArrayList<LocationTimeStamp> lts){
        mlocationTimeStamps = lts;
        notifyItemInserted(0);
    }
}
