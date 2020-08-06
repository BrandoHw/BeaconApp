package org.altbeacon.bluetooth;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.altbeacon.WorkTracking.R;

import java.util.List;

public class AttendanceListAdapter extends RecyclerView.Adapter<AttendanceListAdapter.AttendanceViewHolder>{

    private List<AttendanceRecord> mAttendanceList;

    public class AttendanceViewHolder extends RecyclerView.ViewHolder {

        public final TextView name, date;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.card_att_email);
            date = itemView.findViewById(R.id.card_att_timestamp);
        }
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_attendance, parent, false);
        return new AttendanceViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        if (mAttendanceList != null && position < mAttendanceList.size()){
            AttendanceRecord ar = mAttendanceList.get(position);
            holder.name.setText(ar.getEmail());
            holder.date.setText("[" + ar.getDate() + "]");
        }

    }

    void setAttendanceList(List<AttendanceRecord> studentInfo){
        mAttendanceList = studentInfo;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mAttendanceList != null)
            return mAttendanceList.size();
        else
            return 0;
    }

}
