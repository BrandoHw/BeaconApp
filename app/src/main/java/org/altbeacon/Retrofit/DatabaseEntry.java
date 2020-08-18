package org.altbeacon.Retrofit;

public class DatabaseEntry {
    private String imei, fName, lName, phone_number, avatar;
    private double longitude, latitude, altitude;
    private int pedometer;
    private String address;
    private String battery, heartrate;
    private int trans_interval;
    private String text_admin, alert;
    private int sos;
    private int online;
    private String created_at, updated_at;

    public DatabaseEntry(String imei, String fName, String lName, String phone_number,
                         double longitude, double latitude, String heartrate, int pedometer,
                         String address, String battery, int trans_interval) {
        this.imei = imei;
        this.fName = fName;
        this.lName = lName;
        this.phone_number = phone_number;
        this.longitude = longitude;
        this.latitude = latitude;
        this.heartrate = heartrate;
        this.pedometer = pedometer;
        this.address = address;
        this.battery = battery;
        this.trans_interval = trans_interval;
    }
}