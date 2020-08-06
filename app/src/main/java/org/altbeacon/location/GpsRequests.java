package org.altbeacon.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;

import org.altbeacon.WorkTracking.MainApplication;
import org.altbeacon.utils.NotificationUtils;


public class GpsRequests {

    private static final String TAG = GpsRequests.class.getSimpleName();

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;


    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 30000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 30000;

    public GpsRequests() {

        this.locationRequest = new LocationRequest();
        this.locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        this.locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        this.locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(this.locationRequest);
        this.locationSettingsRequest = builder.build();

        Log.i(TAG, "Creating gps");
        this.locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location currentLocation = locationResult.getLastLocation();

                if (currentLocation != null) {
                    double lat = currentLocation.getLatitude();
                    double lng = currentLocation.getLongitude();
                    Log.i(TAG, "Location Callback results lat: " + lat + "lng: " + lng);
                }
            }
        };

    }

    public LocationSettingsRequest getLocationSettingsRequest() {
        return this.locationSettingsRequest;
    }

    public void start() {
        this.mFusedLocationClient = LocationServices.getFusedLocationProviderClient(MainApplication.getContext());
        Log.i(TAG, "Requesting gps");
        if (ActivityCompat.checkSelfPermission(MainApplication.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainApplication.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            NotificationUtils.createNotification("Request Location Permissions",
                    "Location permissions are required for this app to function properly", 1,
                    MainApplication.getContext(), true);
            return;
        }else {
            this.mFusedLocationClient.requestLocationUpdates(this.locationRequest,
                    this.locationCallback, Looper.myLooper());
        }
    }
    public void stop() {
        Log.i(TAG, "stop() Stopping location tracking");
        if (mFusedLocationClient != null)
        this.mFusedLocationClient.removeLocationUpdates(this.locationCallback);
    }

}