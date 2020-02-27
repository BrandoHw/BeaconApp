package org.altbeacon.WorkTracking;

import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;

import com.ibm.cloud.appid.android.api.AppID;
import com.ibm.cloud.appid.android.api.AppIDAuthorizationManager;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.altbeacon.Network.DatabaseSync;
import org.altbeacon.Network.ServerlessAPI;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.bluetooth.BluetoothMedic;
import org.altbeacon.login.TokensPersistenceManager;
import org.altbeacon.objects.LocationTimeStamp;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;

import java.net.URI;
import java.util.Collection;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 */
public class MainApplication extends Application implements BootstrapNotifier, BeaconConsumer{
    private static final String TAG = "BeaconReferenceApp";
    private static MainApplication mContext;
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;
    private boolean haveDetectedBeaconsSinceBoot = false;
    private MonitoringActivity monitoringActivity = null;
    private BeaconSettingActivity beaconSettingActivity = null;
    private LocationTimestampFragment locationTimestampFragment = null;
    private String cumulativeLog = "";
    private Long tsPeriodic; //This value is used to measure out a periodic interval during which the closest beacon to the device is determined
    private Long tsInRegion, tsOutRegion = null; //These values indicate the time spent in or out of a region respectively
    private String closestLocation = null; //This is the the beacon or area that the device is closest to
    private String previousClosestLocation = null; //This is the previous closest beacon or area which is appended to the user's log
    private String setLocation = null; //This location is specifically set in the maps tab and use only when the user is using GPS to determine their location
    private double closestBeaconDist = 100;
    BeaconManager beaconManager;
    private Identifier myBeaconNamespaceId = Identifier.parse("0x00112233445566778898");

    private ArrayList<LocationTimeStamp> mlocationTimeStamps = new ArrayList<>();
    //LocationTimeStamp lts = new LocationTimeStamp("Testing", getDateCurrentTimeZone(100000), getDurationBreakdown(100000));
    private LinkedHashMap<String, Beacon> beaconList = new LinkedHashMap<String, Beacon>();

    //Alarm Manager
    private int mHours = 9, mMinutes = 0, eHours = 18, eMinutes = 0;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent, alarmIntent2;

    //App ID
    private AppID appID;
    private AppIDAuthorizationManager appIDAuthorizationManager;
    private TokensPersistenceManager tokensPersistenceManager;
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
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


        //Bluetooth Medic, this is responsible for recovering from a full bluetooth stack, resulting in scan failed errors
        //This programmatically turns the bluetooth of the device on and off
        BluetoothMedic medic = BluetoothMedic.getInstance();
        medic.enablePowerCycleOnFailures(this);
        medic.enablePeriodicTests(this, BluetoothMedic.SCAN_TEST | BluetoothMedic.TRANSMIT_TEST);


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
        SharedPreferences sharedPreferences = getSharedPreferences("myNamespace", 0);
        Log.d(TAG, "setting up background monitoring for beacons and power saving");
        // wake up the app when a beacon is seen
        if (sharedPreferences.contains("namespace")) {
            myBeaconNamespaceId = Identifier.parse(sharedPreferences.getString("namespace", getString(R.string.mmdt_no_namespace)));
        }
        Region region = new Region("backgroundRegion",
                myBeaconNamespaceId, null, null);
        regionBootstrap = new RegionBootstrap(this, region);

        // simply constructing this class and holding a reference to it in your custom Application
        // class will automatically cause the BeaconLibrary to save battery whenever the application
        // is not visible.  This reduces bluetooth power usage by about 60%
        backgroundPowerSaver = new BackgroundPowerSaver(this);
        //beaconManager.setBackgroundScanPeriod(60000);
        beaconManager.bind(this);
        // If you wish to test beacon detection in the Android Emulator, you can use code like this:
        // BeaconManager.setBeaconSimulator(new TimedBeaconSimulator() );
        // ((TimedBeaconSimulator) BeaconManager.getBeaconSimulator()).createTimedSimulatedBeacons();

        //Check if alarm is enabled, then set it
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean("alarmEnable", true))
            setAlarm(false);
        else
            cancelAlarm();

        //Load in today's log from the local datastore into mLocationTimestamps
        long currentTime = System.currentTimeMillis();
        mlocationTimeStamps = DatabaseSync.getLogs(getDateOnly(currentTime));

        //Initialize AppID
        appID = AppID.getInstance();
        appID.initialize(this, getString(R.string.authTenantId), AppID.REGION_US_SOUTH);
        appIDAuthorizationManager = new AppIDAuthorizationManager(appID);
        tokensPersistenceManager = new TokensPersistenceManager(this, appIDAuthorizationManager);
    }


    public static MainApplication getContext(){
        return mContext;
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
            SharedPreferences sharedPreferences = getSharedPreferences("myNamespace", 0);
            if (sharedPreferences.contains("namespace")) {
                myBeaconNamespaceId = Identifier.parse(sharedPreferences.getString("namespace", getString(R.string.mmdt_no_namespace)));
            }

            Region region = new Region("backgroundRegion",
                    myBeaconNamespaceId, null, null);
            try {
                beaconManager.stopRangingBeaconsInRegion(region);
            } catch (RemoteException e) {   }
        }
    }
    public void enableMonitoring() {
        SharedPreferences sharedPreferences = getSharedPreferences("myNamespace", 0);
        if (sharedPreferences.contains("namespace")) {
            myBeaconNamespaceId = Identifier.parse(sharedPreferences.getString("namespace", getString(R.string.mmdt_no_namespace)));
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
        if (!haveDetectedBeaconsSinceBoot) {
            Log.d(TAG, "auto launching MainActivity");

            // The very first time since boot that we detect an beacon, we launch the
            // MainActivity
            //Intent intent = new Intent(this, MonitoringActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // Important:  make sure to add android:launchMode="singleInstance" in the manifest
            // to keep multiple copies of this activity from getting created if the user has
            // already manually launched the app.
            //this.startActivity(intent);
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
                //Intent myIntent = new Intent(this, MonitoringActivity.class);
                //myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //this.startActivity(myIntent);
            }

        }
    }

    @Override
    public void didExitRegion(Region region) {
        //logToDisplay("Current location is Outside");
        //if 9 am and not 6pm
        Log.i(TAG, "Exit Triggered");
        //Intent myIntent = new Intent(this, MapsActivityCurrentPlace.class);
        //myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //this.startActivity(myIntent);
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        logToDisplay("Current region state is: " + (state == 1 ? "INSIDE" : "OUTSIDE ("+state+")"));
        tsPeriodic = System.currentTimeMillis();
        Log.i(TAG, "The Region is" + region.toString());
        Log.i("DidDetermine", "Triggered with state: " + state);
        BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
        if (state == 1) {
            try {
                beaconManager.startRangingBeaconsInRegion(region);
            }
            catch (RemoteException e) {
                if (BuildConfig.DEBUG) Log.d(TAG, "Can't start ranging");
            }
            LocalTime now = LocalTime.now(ZoneId.systemDefault());
            if (isBetweenWorkHours((now))) {
                //For outside of region to inside event
                if (tsOutRegion != null && previousClosestLocation != null && closestLocation == "Outside" && previousClosestLocation != closestLocation) {
                    Log.i("DidDetermine", "Outside to Inside");
                    tsInRegion = System.currentTimeMillis();
                    long timeSpent = tsInRegion - tsOutRegion;
                    long currentTime = tsInRegion;
                    //display timespent and current time
                    previousClosestLocation = closestLocation;

                    if (setLocation == null) {
                        Log.i("DidDetermine", "Set Location");
                        appendToList(previousClosestLocation, currentTime, timeSpent);
                        updateFragment();
                        storeLogs(currentTime);
                    }
                    else{
                        Log.i("DidDetermine", "Other");
                        appendToList(setLocation, currentTime, timeSpent);
                        updateFragment();
                        storeLogs(currentTime);
                    }
                    /*
                    String displayCurrentTime = getDateCurrentTimeZone(currentTime);
                    Log.i("Current Time", displayCurrentTime);
                    updateCurrentLocationLTS("Pending Closest Beacon", displayCurrentTime);
                    updateCurrentLocationMaps("Pending Closest Beacon", displayCurrentTime);
                    */
                }
            }
        }
        else {
            /*
            try {
                beaconManager.stopRangingBeaconsInRegion(region);
            } catch (RemoteException e) {
                e.printStackTrace();
            }*/
            //if 9-6
            LocalTime now = LocalTime.now(ZoneId.systemDefault());
            if (isBetweenWorkHours((now))) {
                if (previousClosestLocation == null) {
                    //For starting in outside location
                    Log.i("DidDetermine", "PCL = Null");
                    tsOutRegion = System.currentTimeMillis();
                    String displayCurrentTime = getDateCurrentTimeZone(tsOutRegion);
                    closestLocation = "Outside";
                    updateCurrentLocationLTS(closestLocation, displayCurrentTime);
                    updateCurrentLocationMaps(closestLocation, displayCurrentTime);
                    //For moving from inside to outside
                }else if (tsInRegion != null && previousClosestLocation != null && closestLocation != "Outside") {
                    Log.i("DidDetermine", "Inside to Outside");
                    tsOutRegion = System.currentTimeMillis();
                    long timeSpent = tsOutRegion - tsInRegion;
                    long currentTime = tsOutRegion;
                    Log.i(TAG, "Duration: " + timeSpent + "\nCurrent Time: " + currentTime);
                    //convert current time to date
                    String displayCurrentTime = getDateCurrentTimeZone(currentTime);
                    //display timespent and current time
                    previousClosestLocation = closestLocation;
                    closestLocation = "Outside";
                    appendToList(previousClosestLocation, currentTime, timeSpent);
                    updateFragment();
                    storeLogs(currentTime);
                    updateCurrentLocationLTS(closestLocation, displayCurrentTime);
                    updateCurrentLocationMaps(closestLocation, displayCurrentTime);
                }
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

    public void setBeaconSettingActivity(BeaconSettingActivity activity) {
        this.beaconSettingActivity = activity;
    }

    public void logToDisplay(String line) {
        cumulativeLog += (line + "\n");
        if (this.monitoringActivity != null) {
            //this.monitoringActivity.updateLog(cumulativeLog);
        }
    }

    public String getLog() {
        return cumulativeLog;
    }

    public static String getDurationBreakdown(long millis) {
        if(millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }
        Log.i("Duration", "Miliseconds: " + millis);
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);
        sb.append(hours);
        sb.append("hr ");
        sb.append(minutes);
        sb.append("m ");
        sb.append(seconds);
        sb.append("s");
        Log.i(TAG, sb.toString());
        return(sb.toString());
    }

    public  String getDateCurrentTimeZone(long timestamp) {
        try{
            Calendar calendar = Calendar.getInstance();
            TimeZone tz = TimeZone.getDefault();
            calendar.setTimeInMillis(timestamp);
            //calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date currentTimeZone = (Date) calendar.getTime();
            return sdf.format(currentTimeZone);
        }catch (Exception e) {
        }
        return "";
    }

    public  String getDateOnly(long timestamp) {
        try{
            Calendar calendar = Calendar.getInstance();
            TimeZone tz = TimeZone.getDefault();
            Log.d("Time zone: ", tz.getDisplayName());
            calendar.setTimeInMillis(timestamp);
            //calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date currentTimeZone = (Date) calendar.getTime();
            Log.d("Time: ", sdf.format(currentTimeZone));
            return sdf.format(currentTimeZone);
        }catch (Exception e) {
        }
        return "";
    }

    public boolean isBetweenWorkHours(LocalTime now){
        int mHours = 9, mMinutes = 0, eHours = 18, eMinutes = 0;
        SharedPreferences sharedPreferences = getSharedPreferences("myShift", 0);
        if (sharedPreferences.contains("mHours")) {
            mHours = sharedPreferences.getInt("mHours", 9);
            //Log.i(TAG, "Set Time: " + Integer.toString(mHours));
        }
        if (sharedPreferences.contains("mMinutes")) {
            mMinutes = sharedPreferences.getInt("mMinutes", 0);
            //Log.i(TAG, "Set Time: " + Integer.toString(mMinutes));
        }
        if (sharedPreferences.contains("eHours")) {
            eHours = sharedPreferences.getInt("eHours", 18);
            //Log.i(TAG, "Set Time: " + Integer.toString(eHours));
        }
        if (sharedPreferences.contains("eMinutes")) {
            eMinutes = sharedPreferences.getInt("eMinutes", 0);
            //Log.i(TAG, "Set Time: "+ Integer.toString(eMinutes));
        }
        LocalTime start = LocalTime.of(mHours, mMinutes);
        LocalTime end = LocalTime.of(eHours, eMinutes);
        boolean isBetween = now.isAfter(start) && now.isBefore(end);
        return isBetween;
    }

    public long latestTimeStamp(){
        if (tsInRegion != null && tsOutRegion == null)
            return tsInRegion;
        else if (tsOutRegion != null && tsInRegion == null)
            return tsOutRegion;
        else if (tsOutRegion == null && tsInRegion == null)
            return 1;
        else if (tsInRegion > tsOutRegion)
            return tsInRegion;
        else
            return tsOutRegion;
    }

    public String lastLocation(){
        return  closestLocation;
    }

    public void appendToList(String location, long timestamp,  long milis){
        Log.i(TAG, "appendToList called");
        LocationTimeStamp lts = new LocationTimeStamp(location, getDateCurrentTimeZone(timestamp), getDurationBreakdown(milis));
        mlocationTimeStamps.add(0, lts);
    }

    public void updateFragment() {
        Log.i(TAG, "updateFragment called");
        if (monitoringActivity != null) {
            if (monitoringActivity.isFragmentVisible("LTS_FRAG")) {
                LocationTimestampFragment myFragment = monitoringActivity.returnLtsFragment();
                myFragment.updateAdapter();
                Log.i(TAG, "updateFragment success");
            }
        }
    }

    public void setClosestLocationEnd(){
        closestLocation = "END";
    }
    public void updateCurrentLocationLTS(String currentLocation, String timestamp){
        Log.d(TAG, "updateCurrentLocationLTS called");
        if (monitoringActivity != null) {
            if (monitoringActivity.isFragmentVisible("LTS_FRAG")) {
                LocationTimestampFragment myFragment = monitoringActivity.returnLtsFragment();
                myFragment.updateCurrentLocation(currentLocation, timestamp);
                Log.d(TAG, "updateCurrentLocationLTS success");
            }
        }else{//If monitoring activity is not visible it cannot be updated, store the data temporarily to sp and update in onResume();
            SharedPreferences sp = getSharedPreferences("temp", 0);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("currentLocation", currentLocation);
            editor.putString("timestamp", timestamp);
            editor.commit();
        }
    }

    public void updateCurrentLocationMaps(String currentLocation, String timestamp){
        Log.d(TAG, "updateCurrentLocationMaps called");
        if (monitoringActivity != null) {
            if (monitoringActivity.isFragmentVisible("MAPS_FRAG")) {
                MapsFragment myFragment = monitoringActivity.returnMapsFragment();
                myFragment.updateCurrentLocation(currentLocation, timestamp);
                Log.d(TAG, "updateCurrentLocationMaps success");
            }
        }
    }

    public ArrayList<LocationTimeStamp> locationList(){
        return mlocationTimeStamps;
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager = BeaconManager.getInstanceForApplication(this);
            RangeNotifier rangeNotifier = new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    Log.d(TAG, "didRangeBeaconsInRegion called with beacon count:  " + beacons.size());
                    //if 9-6
                    LocalTime now = LocalTime.now(ZoneId.systemDefault());
                    if (isBetweenWorkHours((now))){
                        //Check if the namespace has changed, clear the beaconList if it has
                        SharedPreferences sp = getSharedPreferences("myNamespace", 0);
                        if (!(myBeaconNamespaceId.toString().equals(sp.getString("namespace", getString(R.string.mmdt_no_namespace))))) {
                            Log.i("Namespace", "Is running");
                            myBeaconNamespaceId = Identifier.parse(sp.getString("namespace", getString(R.string.mmdt_no_namespace)));
                            disableMonitoring();
                            enableMonitoring();
                            beaconList = new LinkedHashMap<String, Beacon>();
                        }
                        SharedPreferences sharedPreferences = getSharedPreferences("myBeacons", 0);

                        //Determine which beacon in range is the closest
                        for (Beacon beacon : beacons) {
                            putListOfBeacons(beacon); //Put beacon into the list of beacons in range, used when editing beacon info in the beacons setting activity
                            Log.i(TAG, "Beacon instance is: "+beacon.getId2().toString());
                            //Check if the beacon instance has been saved as a location in shared preferences
                            if (sharedPreferences.contains(beacon.getId2().toString())) {
                                String location = sharedPreferences.getString(beacon.getId2().toString(), null);
                                double beaconDist = beacon.getDistance();
                                Log.i(TAG, "I see a beacon " + location + "that is about " + beacon.getDistance() + " meters away.");
                                //Log.i(TAG, "The closest beacon is: "+closestBeaconDist+" meters away.");
                                if (beaconDist < closestBeaconDist) {
                                    closestBeaconDist = beaconDist;
                                    closestLocation = location;
                                    Log.i(TAG, "The closest beacon is: " + closestLocation + " meters away.");
                                }

                            }

                        }
                        //Update list of beacons
                        updateBeaconSettingActivity();

                        //Determine closest beacon only after pinging beacons for 10 seconds to account for distance measurement errors
                        if (tsPeriodic + 10000 <= System.currentTimeMillis()) {
                            closestBeaconDist = 100;
                            tsPeriodic = System.currentTimeMillis();
                            Log.i(TAG, "The closest beacon is: " + closestLocation + " The previous cl is " + previousClosestLocation);
                            if (closestLocation != null && !(closestLocation.equals(previousClosestLocation))) {
                                //If this is not the first time a beacon is detected and the previous location was not outside the region
                                // i.e The location has changed from one beacon to another
                                if (previousClosestLocation != null && previousClosestLocation != "Outside") {
                                    Log.i("didRange", "PCL != Null, pcl != outside");
                                    long currentTime = System.currentTimeMillis();
                                    long timeSpent = currentTime - tsInRegion;
                                    //convert current time to date
                                    String displayCurrentTime = getDateCurrentTimeZone(currentTime);
                                    //display timespent and current time
                                    Log.i(TAG, "Append should be called here");
                                    appendToList(previousClosestLocation, currentTime, timeSpent);
                                    updateFragment();
                                    //URI uri = DatabaseSync.getCredentials();
                                    //if (uri != null)
                                     //   DatabaseSync.storeLogs(mlocationTimeStamps, uri, getDateOnly(currentTime));
                                    storeLogs(currentTime);
                                    updateCurrentLocationLTS(closestLocation, displayCurrentTime);
                                    updateCurrentLocationMaps(closestLocation, displayCurrentTime);
                                    tsInRegion = currentTime;
                                    previousClosestLocation = closestLocation;
                                }
                                else if(previousClosestLocation == "Outside"){
                                    Log.i("didRange", "pcl = outside");
                                    long currentTime = tsInRegion;
                                    String displayCurrentTime = getDateCurrentTimeZone(currentTime);
                                    Log.i("Current Time", displayCurrentTime);
                                    updateCurrentLocationLTS(closestLocation, displayCurrentTime);
                                    updateCurrentLocationMaps(closestLocation, displayCurrentTime);
                                    Log.i(TAG, "The current location is " + closestLocation);
                                    previousClosestLocation = closestLocation;
                                }
                                // This logic is used the first time a beacon is detected
                                // i.e The location starts within range of a beacon
                                else if(previousClosestLocation == null) {
                                    Log.i("didRange", "Other");
                                    tsInRegion = System.currentTimeMillis();
                                    long currentTime = tsInRegion;
                                    String displayCurrentTime = getDateCurrentTimeZone(currentTime);
                                    Log.i("Current Time", displayCurrentTime);
                                    updateCurrentLocationLTS(closestLocation, displayCurrentTime);
                                    updateCurrentLocationMaps(closestLocation, displayCurrentTime);
                                    Log.i(TAG, "The current location is " + closestLocation);
                                    previousClosestLocation = closestLocation;
                                }
                            }
                        }
                    }
                }
            }

        };
        beaconManager.removeAllRangeNotifiers();
        beaconManager.addRangeNotifier(rangeNotifier);
    }

    public void putListOfBeacons(Beacon beacon){
        String namespace = beacon.getId1().toString();
        String instance = beacon.getId2().toString();
        beaconList.put((namespace + instance), beacon);
    }

    public LinkedHashMap<String, Beacon> returnBeaconList(){
        return beaconList;
    }

    public void updateBeaconSettingActivity() {
        Log.d(TAG, "updateBeaconSetting called");
        if (beaconSettingActivity != null) {
            beaconSettingActivity.updateBeaconList();
            Log.d(TAG, "updateBeacon success");
        }
    }

    public void setLocation(String setLocation){
        this.setLocation = setLocation;
    }


    public void setAlarm(Boolean forTomorrow) {
        alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        Intent intent2 = new Intent(this, AlarmReceiver.class);
        alarmIntent2 = PendingIntent.getBroadcast(this, 1, intent2, 0);


        Calendar scheduleCalendar = Calendar.getInstance();
        scheduleCalendar.setTimeInMillis(System.currentTimeMillis());
        Calendar scheduleCalendar2 = Calendar.getInstance();
        scheduleCalendar2.setTimeInMillis(System.currentTimeMillis());

        SharedPreferences sharedPreferences = getSharedPreferences("myShift", 0);
        mHours = sharedPreferences.getInt("mHours", 9);
        mMinutes = sharedPreferences.getInt("mMinutes", 0);
        eHours = sharedPreferences.getInt("eHours", 18);
        eMinutes = sharedPreferences.getInt("eMinutes", 0);


        scheduleCalendar.set(Calendar.HOUR_OF_DAY, mHours);
        scheduleCalendar.set(Calendar.MINUTE, mMinutes);
        scheduleCalendar2.set(Calendar.HOUR_OF_DAY, eHours);
        scheduleCalendar2.set(Calendar.MINUTE, eMinutes);

        if (forTomorrow) {
            Log.i("Alarm", "Setting Alarm for tomorrow");
            scheduleCalendar.add(Calendar.DAY_OF_YEAR, 1);
            scheduleCalendar2.add(Calendar.DAY_OF_YEAR, 1);
        }

        alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, scheduleCalendar.getTimeInMillis(), alarmIntent);
        alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, scheduleCalendar2.getTimeInMillis(), alarmIntent2);
        Log.i("Alarm", "Alarm Set");
    }

    public void cancelAlarm() {
        alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        Intent intent2 = new Intent(this, AlarmReceiver.class);
        alarmIntent2 = PendingIntent.getBroadcast(this, 1, intent2, 0);

        alarmMgr.cancel(alarmIntent);
        alarmMgr.cancel(alarmIntent2);
        Log.i("Alarm", "Alarm Set");
    }

    //Whenever a new work day has started the values used to track location and time must be reset
    public void initNewday(){
        Log.i(TAG, "Initialize started");
        tsPeriodic = System.currentTimeMillis();
        tsInRegion = null;
        tsOutRegion = null;
        closestLocation = null;
        previousClosestLocation = null;
        setLocation = null;
        closestBeaconDist = 100;
        mlocationTimeStamps = DatabaseSync.getLogs(getDateOnly(System.currentTimeMillis()));
    }
    public void storeLogs(long currentTime) {
        SharedPreferences sp = getSharedPreferences("myProfile", 0);
        final String role = sp.getString("myRole", "Employee");
        String username = sp.getString("profileName", "no_name_given").toLowerCase();
        final String name = username.replace(' ', '-');
        Log.i(TAG, "Storelogs started");
        //Ensure AppID has logged in a retrieved the name of the user
        if (username != "no_name_given") {
            new org.altbeacon.Network.DoWithoutProgress() {
                @Override
                protected URI doInBackground(URI... params) {
                    try {
                        Log.i(TAG, "Storelogs: serverless api attempt");
                        URI uri = ServerlessAPI.getCredentials(appIDAuthorizationManager.getAccessToken(), name, role);
                        Log.i(TAG, "Storelogs: URI is: " + uri.toString());
                        return uri;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(URI uri) {
                    super.onPostExecute(uri);
                    if (uri != null) {
                        Log.i(TAG, "Storelogs: URI is: " + uri.toString());
                        Log.i(TAG, "Storelogs: push started");
                        DatabaseSync.storeLogs(mlocationTimeStamps, uri, getDateOnly(currentTime));
                    }
                    Log.i(TAG, "Storelogs: Post Execute Finished");
                }
            }.execute();

        }
    }

    public void createDatabase() {
        SharedPreferences sp = getSharedPreferences("myProfile", 0);
        String username = sp.getString("profileName", "no_name_given").toLowerCase();
        final String name = username.replace(' ', '-');
        Log.i("createDatabase", "started");
        //Ensure AppID has logged in a retrieved the name of the user
        if (username != "no_name_given") {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        Log.i("createDatabase", "started");
                        ServerlessAPI.createDatabase(appIDAuthorizationManager.getAccessToken(), name);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    Log.i("Post-Execute", "Finished creating database");
                }
            }.execute();

        }
    }
}


