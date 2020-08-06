package org.altbeacon.bluetooth;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AttendanceDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(AttendanceRecord record);

    @Update
    void update(AttendanceRecord record);

    @Query("SELECT * from attendance_records WHERE date = :date")
    LiveData<List<AttendanceRecord>> getRecordsToday(String date);

    @Query("SELECT * from attendance_records WHERE location = :location AND date = :date")
    List<AttendanceRecord> getRecordForDateLocation(String location, String date);

    @Query("SELECT * from attendance_records WHERE location = :location AND date = :date")
    LiveData<List<AttendanceRecord>> getRecordForDateLocationLive(String location, String date);

    @Query("SELECT * from attendance_records")
    LiveData<List<AttendanceRecord>> getAllRecords();

    @Query("DELETE FROM attendance_records")
    void deleteAll();

}
