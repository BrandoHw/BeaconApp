package org.altbeacon.beaconreference;

import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class BeaconSettingsRecycleViewAdapter extends RecyclerView.Adapter<BeaconSettingsRecycleViewAdapter.BeaconsViewHolder>{

    private static final String TAG = "BeaconRecycler";
    private LinkedHashMap<String, Beacon> mBeaconList= new LinkedHashMap<String, Beacon>();
    private ArrayList<Beacon> mBeaconArray = new ArrayList<Beacon>();
    private BeaconSettingActivity mBeaconSettingActivity;
    private OnCardClickListener mOnClickListener;

    BeaconSettingsRecycleViewAdapter(LinkedHashMap<String, Beacon> beaconList, BeaconSettingActivity beaconSettingActivity, OnCardClickListener onClickListener){
       mBeaconList = beaconList;
       mBeaconSettingActivity = beaconSettingActivity;
       this.mOnClickListener = onClickListener;
    }

    @NonNull
    @Override
    public BeaconsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.beacon_card, parent, false);
        return new BeaconsViewHolder(layoutView, mOnClickListener );
    }

    @Override
    public void onBindViewHolder(@NonNull BeaconsViewHolder holder, int position) {
        Log.i(TAG, "onBindViewHolder called");

        if (!mBeaconList.isEmpty() && position < mBeaconList.size()){
            mBeaconArray = new ArrayList<Beacon>(mBeaconList.values());
            Beacon mBeacon = mBeaconArray.get(position);
            holder.namespace.setText("Namespace ID: " + mBeacon.getId1().toString());
            holder.instanceId.setText("Instance ID: " + mBeacon.getId2());
            Log.i(TAG, mBeacon.getId1().toString());
            SharedPreferences sp = mBeaconSettingActivity.getSharedPreferences("myBeacons", 0);
            String location = sp.getString(mBeacon.getId2().toString(), "No Location Set");
            holder.location.setText("Given Location Name: " + location);
        }

    }

    @Override
    public int getItemCount() {
        return mBeaconArray.size();
    }

    public class BeaconsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView namespace;
        public TextView instanceId;
        public TextView location;
        OnCardClickListener onClickListener;

        public BeaconsViewHolder(@NonNull View itemView, OnCardClickListener onClickListener) {
            super(itemView);
            namespace = itemView.findViewById(R.id.namespace);
            instanceId = itemView.findViewById(R.id.instanceId);
            location = itemView.findViewById(R.id.location);

            this.onClickListener = onClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onClickListener.onCardClick(getAdapterPosition());
        }
    }

    public ArrayList<Beacon> update(LinkedHashMap<String, Beacon> beaconList){
        mBeaconList = beaconList;
        Log.i(TAG, "The size is: " + mBeaconList.size());
        mBeaconArray = new ArrayList<Beacon>(mBeaconList.values());
        notifyDataSetChanged();
        return mBeaconArray;
    }

    public interface OnCardClickListener{
        void onCardClick(int position);
    }
}
