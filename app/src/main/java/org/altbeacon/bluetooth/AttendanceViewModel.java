package org.altbeacon.bluetooth;

import android.app.Application;
import android.location.Location;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;


import java.util.List;

public class AttendanceViewModel extends AndroidViewModel {

    private static final String TAG = "AttendanceVM";
    private AttendanceRepository mRepository;

    private LiveData<List<AttendanceRecord>> mAllRecords;
    private LiveData<List<AttendanceRecord>> mRecordsToday;


    private MutableLiveData<String> loadTrigger;
    private LiveData<List<AttendanceRecord>> mRecordsTodayForLocation;


    public AttendanceViewModel (Application application) {
        super(application);
        mRepository = new AttendanceRepository(application);
        loadTrigger = new MutableLiveData<>();
        mRecordsToday = mRepository.getAllRecordsToday();
        mAllRecords = mRepository.getAllRecords();
        mRecordsTodayForLocation = Transformations.switchMap(
                loadTrigger,
                (location) -> mRepository.getAllRecordsTodayForLocation(location));
    }


    LiveData<List<AttendanceRecord>> getAllRecords() { return mAllRecords;}

    LiveData<List<AttendanceRecord>> getRecordsToday() { return mRecordsToday; }

    public void insert(AttendanceRecord attendanceRecord) { mRepository.insert(attendanceRecord); }

    public void update(String location) {mRepository.update(location);}

    public LiveData<List<AttendanceRecord>> getRecordsTodayForLocation() {
        return mRecordsTodayForLocation;
    }

    public void refresh(String location){
        Log.i(TAG, "refresh called" + location);
        loadTrigger.setValue(location);
        //loadTrigger.setValue("refresh");
    }


}
