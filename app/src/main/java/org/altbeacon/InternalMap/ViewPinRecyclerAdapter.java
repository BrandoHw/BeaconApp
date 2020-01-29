package org.altbeacon.InternalMap;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beaconreference.BeaconSettingActivity;
import org.altbeacon.beaconreference.R;
import org.altbeacon.network.LocationTimeStamp;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ViewPinRecyclerAdapter extends RecyclerView.Adapter<ViewPinRecyclerAdapter.PinViewHolder>{

    private static final String TAG = "ViewPinRecycler";
    private ArrayList<EmployeeCard> mEmployeeArray = new ArrayList<EmployeeCard>();
    private ViewPinActivity mViewPinActivity;
    private OnCardClickListener mOnClickListener;

    ViewPinRecyclerAdapter(ViewPinActivity viewPinActivity, OnCardClickListener onClickListener){
        mViewPinActivity = viewPinActivity;
        this.mOnClickListener = onClickListener;
    }

    @NonNull
    @Override
    public PinViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.employee_card, parent, false);
        return new PinViewHolder(layoutView, mOnClickListener );
    }

    @Override
    public void onBindViewHolder(@NonNull PinViewHolder holder, int position) {
        Log.i(TAG, "onBindViewHolder called");

        if (!mEmployeeArray.isEmpty() && position < mEmployeeArray.size()){
            EmployeeCard mEmployee = mEmployeeArray.get(position);
            holder.name.setText("Name: " + mEmployee.getName());
            holder.job.setText("Job: " + mEmployee.getJob());
            holder.duration.setText("Duration: " + mEmployee.getDuration());
        }

    }

    @Override
    public int getItemCount() {
        return mEmployeeArray.size();
    }

    public class PinViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView name;
        public TextView job;
        public TextView duration;
        OnCardClickListener onClickListener;

        public PinViewHolder(@NonNull View itemView, OnCardClickListener onClickListener) {
            super(itemView);
            name = itemView.findViewById(R.id.employee_name);
            job = itemView.findViewById(R.id.employee_job);
            duration = itemView.findViewById(R.id.employee_duration);

            this.onClickListener = onClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onClickListener.onCardClick(getAdapterPosition());
        }
    }

    public ArrayList<EmployeeCard> update(ArrayList<EmployeeCard> employeeCards){
        mEmployeeArray = employeeCards;
        notifyDataSetChanged();
        return mEmployeeArray;
    }


    public interface OnCardClickListener{
        void onCardClick(int position);
    }
}