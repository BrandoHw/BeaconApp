package org.altbeacon.beaconreference;

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import android.os.RemoteException;
import android.util.Log;

import com.jakewharton.threetenabp.AndroidThreeTen;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;

import java.util.Collection;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 */
public class BeaconReferenceApplication extends Application implements BootstrapNotifier {
    private static final String TAG = "BeaconReferenceApp";
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;
    private boolean haveDetectedBeaconsSinceBoot = false;
    private MonitoringActivity monitoringActivity = null;
    private String cumulativeLog = "";
    private Long tsPeriodic;
    private Long tsInRegion, tsOutRegion = null;
    private String closestLocation = null;
    private String previousClosestLocation = null;
    private double closestBeaconDist = 100;

    Identifier myBeaconNamespaceId = Identifier.parse("0x00112233445566778898");

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);
        BeaconManager beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);
        // By default the AndroidBeaconLibrary will only find AltBeacons.  If you wish to make it
        // find a different type of beacon, you must specify the byte layout for that beacon's
        // advertisement with a line like below.  The example shows how to find a beacon with the
        // same byte layout as AltBeacon but with a beaconTypeCode of 0xaabb.  To find the proper
        // layout expression for other beacon types, do a web search for "setBeaconLayout"
        // including the quotes.
        //
        //beaconManager.getBeaconParsers().clear();
        //beaconManager.getBeaconParsers().add(new BeaconParser().
        //        setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));

        beaconManager.setDebug(true);

        SharedPreferences sharedPreferences = getSharedPreferences("myBeacons", 0);

        // Uncomment the code below to use a foreground service to scan for beacons. This unlocks
        // the ability to continually scan for long periods of time in the background on Andorid 8+
        // in exchange for showing an icon at the top of the screen and a always-on notification to
        // communicate to users that your app is using resources in the background.
        //

        /*
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentTitle("Scanning for Beacons");
        Intent intent = new Intent(this, MonitoringActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(pendingIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("My Notification Channel ID",
                    "My Notification Name", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("My Notification Channel Description");
            NotificationManager notificationManager = (NotificationManager) getSystemService(
                    Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channel.getId());
        }
        beaconManager.enableForegroundServiceScanning(builder.build(), 456);

        // For the above foreground scanning service to be useful, you need to disable
        // JobScheduler-based scans (used on Android 8+) and set a fast background scan
        // cycle that would otherwise be disallowed by the operating system.
        //
        beaconManager.setEnableScheduledScanJobs(false);
        beaconManager.setBackgroundBetweenScanPeriod(0);
        beaconManager.setBackgroundScanPeriod(1100);
        */

        Log.d(TAG, "setting up background monitoring for beacons and power saving");
        // wake up the app when a beacon is seen
        if (sharedPreferences.contains("namespace")) {
            myBeaconNamespaceId = Identifier.parse(sharedPreferences.getString("namespace", null));
        }
        Region region = new Region("backgroundRegion",
                myBeaconNamespaceId, null, null);
        regionBootstrap = new RegionBootstrap(this, region);

        // simply constructing this class and holding a reference to it in your custom Application
        // class will automatically cause the BeaconLibrary to save battery whenever the application
        // is not visible.  This reduces bluetooth power usage by about 60%
        backgroundPowerSaver = new BackgroundPowerSaver(this);
        beaconManager.setBackgroundScanPeriod(60000);
        // If you wish to test beacon detection in the Android Emulator, you can use code like this:
        // BeaconManager.setBeaconSimulator(new TimedBeaconSimulator() );
        // ((TimedBeaconSimulator) BeaconManager.getBeaconSimulator()).createTimedSimulatedBeacons();
    }

/*    public void scheduler() {
        if not in 9-6
                then disable bootstrap, and all monitoring and ranging and gps
                else enable all monitoring and ranging and gps
    }*/
    public void disableMonitoring() {
        if (regionBootstrap != null) {
            regionBootstrap.disable();
            regionBootstrap = null;
            BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
            SharedPreferences sharedPreferences = getSharedPreferences("myBeacons", 0);
            if (sharedPreferences.contains("namespace")) {
                myBeaconNamespaceId = Identifier.parse(sharedPreferences.getString("namespace", null));
            }

            Region region = new Region("backgroundRegion",
                    myBeaconNamespaceId, null, null);
            try {
                beaconManager.stopRangingBeaconsInRegion(region);
            } catch (RemoteException e) {   }
        }
    }
    public void enableMonitoring() {
        SharedPreferences sharedPreferences = getSharedPreferences("myBeacons", 0);
        if (sharedPreferences.contains("namespace")) {
            myBeaconNamespaceId = Identifier.parse(sharedPreferences.getString("namespace", null));
        }

        Region region = new Region("backgroundRegion",
                myBeaconNamespaceId, null, null);
        regionBootstrap = new RegionBootstrap(this, region);
    }


    @Override
    public void didEnterRegion(Region region) {
        // In this example, this class sends a notification to the user whenever a Beacon
        // matching a Region (defined above) are first seen.
        Log.i(TAG, "did enter region.");
        previousClosestLocation = "Outside";
        if (!haveDetectedBeaconsSinceBoot) {
            Log.d(TAG, "auto launching MainActivity");

            // The very first time since boot that we detect an beacon, we launch the
            // MainActivity
            Intent intent = new Intent(this, MonitoringActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // Important:  make sure to add android:launchMode="singleInstance" in the manifest
            // to keep multiple copies of this activity from getting created if the user has
            // already manually launched the app.
            this.startActivity(intent);
            haveDetectedBeaconsSinceBoot = true;

        } else {
            if (monitoringActivity != null) {
                // If the Monitoring Activity is visible, we log info about the beacons we have
                // seen on its display
                logToDisplay("I see a beacon again");

            } else {
                // If we have already seen beacons before, but the monitoring activity is not in
                // the foreground, we send a notification to the user on subsequent detections.
                Log.d(TAG, "Sending notification.");
                sendNotification();
                Intent myIntent = new Intent(this, MonitoringActivity.class);
                myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(myIntent);
            }

        }
    }

    @Override
    public void didExitRegion(Region region) {
        //logToDisplay("Current location is Outside");
        //if 9 am and not 6pm
        Log.i(TAG, "Exit Triggered");
        closestLocation = "Outside";
        Intent myIntent = new Intent(this, MapsActivityCurrentPlace.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(myIntent);
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        logToDisplay("Current region state is: " + (state == 1 ? "INSIDE" : "OUTSIDE ("+state+")"));
        tsPeriodic = System.currentTimeMillis();
        Log.i(TAG, "The Region is" + region.toString());
        if (state == 1) {
            //if 9-6
            LocalTime now = LocalTime.now(ZoneId.systemDefault());
            if (isBetweenWorkHours((now))) {
                tsInRegion = System.currentTimeMillis();
                if (tsOutRegion != null && previousClosestLocation != null && previousClosestLocation != closestLocation) {
                    long timeSpent = tsInRegion - tsOutRegion;
                    long currentTime = tsInRegion;
                    String displayTimeSpent = getDurationBreakdown(timeSpent);
                    //convert current time to date
                    String displayCurrentTime = getDateCurrentTimeZone(currentTime);
                    //display timespent and current time
                    previousClosestLocation = closestLocation;
                    logToDisplay("[" + displayCurrentTime + "] Employee was in: " + previousClosestLocation + " Duration: " + displayTimeSpent);
                    //logToDisplay("[" + displayCurrentTime + "] Employee's current location is: " + closestLocation);
                }
            }
        }
        else {
            //if 9-6
            LocalTime now = LocalTime.now(ZoneId.systemDefault());
            if (isBetweenWorkHours((now))) {
                previousClosestLocation = closestLocation;
                closestLocation = "Outside";
                tsOutRegion = System.currentTimeMillis();
                if (tsInRegion != null && previousClosestLocation != null && previousClosestLocation != closestLocation) {
                    long timeSpent = tsOutRegion - tsInRegion;
                    long currentTime = tsInRegion;
                    String displayTimeSpent = getDurationBreakdown(timeSpent);
                    //convert current time to date
                    String displayCurrentTime = getDateCurrentTimeZone(currentTime);
                    //display timespent and current time
                    displayTimeStampAndDuration(displayCurrentTime, displayTimeSpent);
                    //previousClosestLocation = "Outside";
                }
            }
        }
        //Start ranging once an enter event has occurred
        BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
        RangeNotifier rangeNotifier = new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    Log.d(TAG, "didRangeBeaconsInRegion called with beacon count:  " + beacons.size());
                    //Beacon firstBeacon = beacons.iterator().next();
                    //logToDisplay("The first beacon " + firstBeacon.toString() + " is about " + firstBeacon.getDistance() + " meters away.");
                    //if 9-6
                    LocalTime now = LocalTime.now(ZoneId.systemDefault());

                    if (isBetweenWorkHours((now))){
                        SharedPreferences sharedPreferences = getSharedPreferences("myBeacons", 0);
                        for (Beacon beacon : beacons) {
                            //Log.i(TAG, "Beacon instance is: "+beacon.getId2().toString());
                            if (sharedPreferences.contains(beacon.getId2().toString())) {
                                String location = sharedPreferences.getString(beacon.getId2().toString(), null);
                                double beaconDist = beacon.getDistance();
                                Log.i(TAG, "I see a beacon that is about " + beacon.getDistance() + " meters away.");
                                //Log.i(TAG, "The closest beacon is: "+closestBeaconDist+" meters away.");
                                if (beaconDist < closestBeaconDist) {
                                    closestBeaconDist = beaconDist;
                                    closestLocation = location;
                                    Log.i(TAG, "The closest beacon is: " + closestLocation + " meters away.");
                                }

                            }

                        }
                        //Determine closest beacon only after pinging beacons for 5 seconds to account for distance measurement errors
                        if (tsPeriodic + 10000 <= System.currentTimeMillis()) {
                            closestBeaconDist = 100;
                            tsPeriodic = System.currentTimeMillis();
                            if (closestLocation != null && (closestLocation != previousClosestLocation)) {
                                //If this is not the first location change and the previous location was not outside the office
                                if (previousClosestLocation != null && previousClosestLocation != "Outside") {
                                    long currentTime = System.currentTimeMillis();
                                    long timeSpent = currentTime - tsInRegion;
                                    String displayTimeSpent = getDurationBreakdown(timeSpent);
                                    //convert current time to date
                                    String displayCurrentTime = getDateCurrentTimeZone(currentTime);
                                    //display timespent and current time
                                    displayTimeStampAndDuration(displayCurrentTime, displayTimeSpent);
                                    tsInRegion = currentTime;
                                    previousClosestLocation = closestLocation;
                                } else {
                                    //logToDisplay("The closest beacon is " + closestBeacon.toString() + " is about " + closestBeacon.getDistance() + " meters away.");
                                    long currentTime = tsInRegion;
                                    String displayCurrentTime = getDateCurrentTimeZone(currentTime);
                                    logToDisplay("[" + displayCurrentTime + "] Employee's current location is: " + closestLocation);
                                    Log.i(TAG, "The current location is " + closestLocation);
                                    previousClosestLocation = closestLocation;
                                }
                            }
                        }
                    }
                }
            }

        };
        LocalTime now = LocalTime.now(ZoneId.systemDefault());
        if (isBetweenWorkHours((now))) {
            try {
                beaconManager.removeAllRangeNotifiers();
                beaconManager.startRangingBeaconsInRegion(region);
                beaconManager.addRangeNotifier(rangeNotifier);
            } catch (RemoteException e) {
            }
        }
    }


    private void sendNotification() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setContentTitle("Beacon Reference Application")
                        .setContentText("An beacon is nearby.")
                        .setSmallIcon(R.drawable.ic_launcher);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(new Intent(this, MonitoringActivity.class));
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    public void setMonitoringActivity(MonitoringActivity activity) {
        this.monitoringActivity = activity;
    }

    public void logToDisplay(String line) {
        cumulativeLog += (line + "\n");
        if (this.monitoringActivity != null) {
            this.monitoringActivity.updateLog(cumulativeLog);
        }
    }

    public String getLog() {
        return cumulativeLog;
    }

    public static String getDurationBreakdown(long millis) {
        if(millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);
        sb.append(hours);
        sb.append(" Hours ");
        sb.append(minutes);
        sb.append(" Minutes ");
        sb.append(seconds);
        sb.append(" Seconds");

        return(sb.toString());
    }
    public  String getDateCurrentTimeZone(long timestamp) {
        try{
            Calendar calendar = Calendar.getInstance();
            TimeZone tz = TimeZone.getDefault();
            Log.d("Time zone: ", tz.getDisplayName());
            calendar.setTimeInMillis(timestamp);
            //calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date currentTimeZone = (Date) calendar.getTime();
            Log.d("Time: ", sdf.format(currentTimeZone));
            return sdf.format(currentTimeZone);
        }catch (Exception e) {
        }
        return "";
    }

    public void displayTimeStampAndDuration(String displayCurrentTime, String displayTimeSpent) {
        logToDisplay("[" + displayCurrentTime + "] Employee was in: " + previousClosestLocation + " Duration: " + displayTimeSpent);
        logToDisplay("[" + displayCurrentTime + "] Employee's current location is: " + closestLocation);
    }

    public boolean isBetweenWorkHours(LocalTime now){
        int mHours = 9, mMinutes = 0, eHours = 18, eMinutes = 0;
        SharedPreferences sharedPreferences = getSharedPreferences("myBeacons", 0);
        if (sharedPreferences.contains("morningHours")) {
            mHours = Integer.parseInt(sharedPreferences.getString("morningHours", null));
            //Log.i(TAG, "Set Time: " + Integer.toString(mHours));
        }
        if (sharedPreferences.contains("morningMinutes")) {
            mMinutes = Integer.parseInt(sharedPreferences.getString("morningMinutes", null));
            //Log.i(TAG, "Set Time: " + Integer.toString(mMinutes));
        }
        if (sharedPreferences.contains("eveningHours")) {
            eHours = Integer.parseInt(sharedPreferences.getString("eveningHours", null));
            //Log.i(TAG, "Set Time: " + Integer.toString(eHours));
        }
        if (sharedPreferences.contains("eveningMinutes")) {
            eMinutes = Integer.parseInt(sharedPreferences.getString("eveningMinutes", null));
            //Log.i(TAG, "Set Time: "+ Integer.toString(eMinutes));
        }
        LocalTime start = LocalTime.of(mHours, mMinutes);
        LocalTime end = LocalTime.of(eHours, eMinutes);
        boolean isBetween = now.isAfter(start) && now.isBefore(end);
        return isBetween;
    }

    public long latestTimeStamp(){
        if (tsInRegion > tsOutRegion)
            return tsInRegion;
        else
            return tsOutRegion;
    }

    public String lastLocation(){
        return  closestLocation;
    }
}


