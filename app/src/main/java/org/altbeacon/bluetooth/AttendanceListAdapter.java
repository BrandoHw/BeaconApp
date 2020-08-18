package org.altbeacon.bluetooth;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.altbeacon.WorkTracking.R;
import org.altbeacon.utils.DateStringUtils;

import java.util.List;

public class AttendanceListAdapter extends RecyclerView.Adapter<AttendanceListAdapter.AttendanceViewHolder>{

    private List<AttendanceRecord> mAttendanceList;
    Context context;
    private static final String TAG = "AttAdapt";
    public class AttendanceViewHolder extends RecyclerView.ViewHolder {

        public final TextView email, check_in, check_out, hours, location;
        public final ImageView status;
        public final MaterialButton sync_button;
        public final MaterialCardView mcv;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            email = itemView.findViewById(R.id.card_att_email);
            check_in = itemView.findViewById(R.id.card_att_check_in);
            check_out = itemView.findViewById(R.id.card_att_check_out);
            hours = itemView.findViewById(R.id.card_att_hours);
            location = itemView.findViewById(R.id.card_att_location);
            status = itemView.findViewById(R.id.card_att_image_status);
            sync_button = itemView.findViewById(R.id.card_att_button_sync);
            mcv = (MaterialCardView) itemView;
        }
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_attendance, parent, false);
        context = parent.getContext();
        return new AttendanceViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        if (mAttendanceList != null && position < mAttendanceList.size()){
            AttendanceRecord ar = mAttendanceList.get(position);

            Log.i(TAG, "Location: " + ar.getLocation() +
                    " Check in: " + ar.getCheck_in() +
                    " Status " + ar.getStatus());
            if (ar.getCheck_out() != null)
                Log.i(TAG, "Check out: " + ar.getCheck_out());

            holder.email.setText(ar.getEmail());
            holder.check_in.setText(ar.getCheck_in());
            if (ar.getStatus() >= 2){
                holder.check_out.setText(ar.getCheck_out());
                holder.hours.setText(DateStringUtils.getDurationBreakdown(ar.getHours_worked()*1000));
                holder.mcv.setStrokeColor(ContextCompat.getColor(context, R.color.md_green_500));
                holder.status.setImageResource(R.drawable.ic_baseline_check_circle_24);
            } else if (ar.getStatus() == 1){
                holder.status.setImageResource(R.drawable.ic_baseline_cancel_24);
                Log.i(TAG, "Image: " + "Red");
                holder.mcv.setStrokeColor(ContextCompat.getColor(context, R.color.md_red_800));
                Log.i(TAG, "Stroke: " + "Red");
            }
            holder.location.setText(ar.getLocation());
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
