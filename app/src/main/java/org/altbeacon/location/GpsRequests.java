package org.altbeacon.location;

import android.location.Location;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;

import org.altbeacon.WorkTracking.MainApplication;


public class GpsRequests{

    private static final GpsRequests instance = new GpsRequests();

    private static final String TAG = GpsRequests.class.getSimpleName();

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;


    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 1000;

    private GpsRequests() {
        this.locationRequest = new LocationRequest();
        this.locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        this.locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        this.locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(this.locationRequest);
        this.locationSettingsRequest = builder.build();

        this.locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location currentLocation = locationResult.getLastLocation();

                double lat = currentLocation.getLatitude();
                double lng = currentLocation.getLongitude();
                Log.i(TAG, "Location Callback results lat: " + lat + "lng: " + lng);

            }
        };

        //this.mFusedLocationClient = LocationServices.getFusedLocationProviderClient(MainApplication.getAppContext());
        this.mFusedLocationClient.requestLocationUpdates(this.locationRequest,
                this.locationCallback, Looper.myLooper());
    }

    public static GpsRequests instance() {
        return instance;
    }

    public LocationSettingsRequest getLocationSettingsRequest() {
        return this.locationSettingsRequest;
    }

    public void stop() {
        Log.i(TAG, "stop() Stopping location tracking");
        this.mFusedLocationClient.removeLocationUpdates(this.locationCallback);
    }

}