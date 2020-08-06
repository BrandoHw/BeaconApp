package org.altbeacon.bluetooth;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {AttendanceRecord.class}, version = 1, exportSchema = false)
public abstract class AttendanceDatabase extends RoomDatabase {
    public abstract AttendanceDao AttendanceDao();

    private static volatile AttendanceDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static AttendanceDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AttendanceDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AttendanceDatabase.class, "qr_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);

            // If you want to keep data through app restarts,
            // comment out the following block
            databaseWriteExecutor.execute(() -> {
                // Populate the database in the background.
                // If you want to start with more words, just add them.

                //TODO: IF database entries are a day old then DELETE all
                AttendanceDao dao = INSTANCE.AttendanceDao();
//                AttendanceDao info  = dao.getLastRecord();

//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                Date dateParsed;
//                Log.i("SQL", "Current: " + System.currentTimeMillis() + "Date: ");
//
//                if (info.getDatetime() != null) {
//                    try {
//                        dateParsed = sdf.parse(info.getDatetime());
//                        Log.i("SQL", "Current: " + System.currentTimeMillis() + "Date: " + dateParsed.getTime());
//                        if (!(DateUtils.isToday(dateParsed.getTime()))) {
//                            dao.deleteAll();
//                        }
//                    } catch (ParseException pe) {
//                        pe.printStackTrace();
//                    }
//                }

            });
        }
    };


}
