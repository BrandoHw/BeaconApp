package org.altbeacon.objects;

/**
 * A Time stamp entry that notes the time and employee left a location, including the duration an
 * employee was at the location
 */
public class LocationTimeStamp {
    private static final String TAG = LocationTimeStamp.class.getSimpleName();

    public final String location;
    public final String timeStamp;
    public final String duration;

    public LocationTimeStamp(
            String location, String timeStamp, String duration) {
        this.location = location;
        this.timeStamp = timeStamp;
        this.duration = duration;
    }

}