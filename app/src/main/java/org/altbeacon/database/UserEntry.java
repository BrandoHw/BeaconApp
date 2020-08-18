package org.altbeacon.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_latest")
public class UserEntry {


    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "imei")
    private String imei;

    @ColumnInfo(name = "full_name")
    private String fName;
    @ColumnInfo(name = "email")
    private String lName;
    @ColumnInfo(name = "phone_number")
    private String phone_number;
    @ColumnInfo(name = "is_inside")
    private boolean is_inside;
    @ColumnInfo(name = "location")
    private String location;
    @ColumnInfo(name = "longitude")
    private double longitude;
    @ColumnInfo(name = "latitude")
    private double latitude;
    @ColumnInfo(name = "address")
    private String address;
    @ColumnInfo(name = "battery")
    private String battery;
    @ColumnInfo(name = "trans_interval")
    private int trans_interval;
    @ColumnInfo(name = "info")
    private String info;
    @ColumnInfo(name = "created_at")
    private String created_at;
    @ColumnInfo(name = "updated_at")
    private String updated_at;

    public UserEntry(String imei, String full_name, String email,
                  int trans_interval, String created_at) {
        this.imei = imei;
        this.fName = full_name;
        this.lName = email;
        this.trans_interval = trans_interval;
        this.created_at = created_at;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public void setIs_inside(boolean is_inside) {
        this.is_inside = is_inside;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }
}