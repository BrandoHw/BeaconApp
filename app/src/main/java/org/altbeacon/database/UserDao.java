package org.altbeacon.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import org.altbeacon.bluetooth.AttendanceRecord;

import java.util.List;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(UserEntry record);

    @Update
    void update(UserEntry record);

    @Query("SELECT * from user_latest ")
    UserEntry getUser();
}
