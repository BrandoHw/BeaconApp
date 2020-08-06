package org.altbeacon.bluetooth;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "attendance_records")
public class AttendanceRecord {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "id")
    private int id = 0;

    @ColumnInfo(name = "email")
    private String email;
    @ColumnInfo(name = "date")
    private String date;
    @ColumnInfo(name = "location")
    private String location;
    @ColumnInfo(name = "check_in")
    private String check_in;
    @ColumnInfo(name = "check_out")
    private String check_out;

    /**
    Status Codes:
                1: Checked in, without check out
                2: Checked out, external database not updated
                3: Checked out, external database updated
     */

    @ColumnInfo(name = "status")
    private int status;
    @ColumnInfo(name = "hours_worked")
    private int hours_worked;


    public AttendanceRecord(String email, String date, String location, String check_in, int status) {
        this.email = email;
        this.date = date;
        this.location = location;
        this.check_in = check_in;
        this.status = status;
    }

    public void checkOut(String check_out){
        if(this.check_in != null){
            this.check_out = check_out;
            this.hours_worked = getHoursWorked(this.check_in, this.check_out);
        }
    }

    private int getHoursWorked(String check_in, String check_out){
        long milis = 10;
        return 10;
    }

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }

    public String getCheck_in() {
        return check_in;
    }

    public String getCheck_out() {
        return check_out;
    }

    public int getStatus() {
        return status;
    }

    public int getHours_worked() {
        return hours_worked;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCheck_out(String check_out) {
        this.check_out = check_out;
    }

    public void setHours_worked(int hours_worked) {
        this.hours_worked = hours_worked;
    }
}
