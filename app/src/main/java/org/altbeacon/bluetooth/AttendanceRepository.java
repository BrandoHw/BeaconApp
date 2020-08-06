package org.altbeacon.bluetooth;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;


import org.altbeacon.WorkTracking.MainApplication;
import org.altbeacon.utils.DateStringUtils;


import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class AttendanceRepository {

    private static final String TAG = "AttRepo";

    private AttendanceDao mAttendanceDao;
    private LiveData<List<AttendanceRecord>> mAllAttendance;
    private LiveData<List<AttendanceRecord>> mAllAttendanceToday;
    private LiveData<List<AttendanceRecord>> mAllAttendanceTodayForLocation;

    // Note that in order to unit test the WordRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples

    AttendanceRepository(Application application) {
        AttendanceDatabase db = AttendanceDatabase.getDatabase(application);
        mAttendanceDao = db.AttendanceDao();
        mAllAttendanceToday = mAttendanceDao.getRecordsToday(DateStringUtils.getDateOnly(System.currentTimeMillis()));
        mAllAttendance = mAttendanceDao.getAllRecords();

    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.

    LiveData<List<AttendanceRecord>> getAllRecords() {
        return mAllAttendance;
    }

    LiveData<List<AttendanceRecord>> getAllRecordsToday() {
        return mAllAttendanceToday;
    }

    LiveData<List<AttendanceRecord>> getAllRecordsTodayForLocation(String location) {
        Log.i(TAG, "List for today for live location: " + location);
        Future<?> future = AttendanceDatabase.databaseWriteExecutor.submit(() -> {
            mAllAttendanceTodayForLocation = mAttendanceDao.getRecordForDateLocationLive(location,
                    DateStringUtils.getDateOnly(System.currentTimeMillis()));
        });

        try {
            future.get(10, TimeUnit.SECONDS);
        } catch(InterruptedException ie) {
            ie.printStackTrace();
        } catch(ExecutionException ee) {
            ee.printStackTrace();
        } catch(TimeoutException te) {
            te.printStackTrace();
        }

        return mAllAttendanceTodayForLocation;
    }


    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    void insert(AttendanceRecord attendanceRecord) {
        AttendanceDatabase.databaseWriteExecutor.execute(() -> mAttendanceDao.insert(attendanceRecord));
    }

    void update(String location) {
        AttendanceDatabase.databaseWriteExecutor.execute(() -> {
            List<AttendanceRecord> list =  mAttendanceDao.getRecordForDateLocation(location, DateStringUtils.getDateOnly(System.currentTimeMillis()));
            SharedPreferences sp = MainApplication.getContext().getSharedPreferences("myProfile", 0);
            String email = sp.getString("profileEmail", "EMAIL NOT FOUND");

            if (list != null) {
                Log.i(TAG, "List for today for location: " + list);
                if (list.isEmpty()) {
                    mAttendanceDao.insert(new AttendanceRecord(email,
                            DateStringUtils.getDateOnly(System.currentTimeMillis()),
                            location,
                            DateStringUtils.getDateCurrentTimeZone(System.currentTimeMillis()),
                            1));
                    Log.i(TAG, "Inserted new attendance record");
                } else {
                    AttendanceRecord updatedRecord;
                    boolean shouldInsert = true;
                    for (AttendanceRecord record : list) {
                        Log.i(TAG, "Status: " + record.getStatus());
                        if (record.getStatus() == 1) {
                            updatedRecord = record;
                            updatedRecord.checkOut(DateStringUtils.getDateCurrentTimeZone(System.currentTimeMillis()));
                            updatedRecord.setStatus(2);
                            mAttendanceDao.update(updatedRecord);
                            Log.i(TAG, "Updated new attendance record");
                            shouldInsert = false;
                        }
                        //else if checked out but not updated create a new check-in entry
                    }
                    if (shouldInsert){
                        mAttendanceDao.insert(new AttendanceRecord(email,
                                DateStringUtils.getDateOnly(System.currentTimeMillis()),
                                location,
                                DateStringUtils.getDateCurrentTimeZone(System.currentTimeMillis()),
                                1));
                    }
                }
            }
        });
    }

}
