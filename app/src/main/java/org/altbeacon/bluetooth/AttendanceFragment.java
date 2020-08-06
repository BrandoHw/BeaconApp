package org.altbeacon.bluetooth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.altbeacon.WorkTracking.R;

import java.util.List;

public class AttendanceFragment extends Fragment {

    AttendanceListAdapter adapter;
    AttendanceViewModel mAttendanceViewModel;

    public static AttendanceFragment newInstance() {
        return new AttendanceFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_attendance_list, container, false);

        // Set up the RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new AttendanceListAdapter();
        recyclerView.setAdapter(adapter);


        mAttendanceViewModel = new ViewModelProvider(getActivity()).get(AttendanceViewModel.class);

        mAttendanceViewModel.getRecordsToday().observe(getActivity(), new Observer<List<AttendanceRecord>>() {
            @Override
            public void onChanged(List<AttendanceRecord> attendanceRecords) {
                adapter.setAttendanceList(attendanceRecords);
            }
        });
        return view;

    }
}
