package org.altbeacon.WorkTracking;

import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import android.os.AsyncTask;
import android.os.Build;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

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
import org.altbeacon.location.GpsRequests;
import org.altbeacon.login.TokensPersistenceManager;
import org.altbeacon.objects.LocationTimeStamp;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;

import java.lang.ref.WeakReference;
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
    private String cumulativeLog = "";
    private Long time_periodic; //This value is used to measure out a periodic interval during which the closest beacon to the device is determined
    private long time_of_entry; //The time at which the device has entered or left a beacon region
    private String closestLocation = null; //This is the the beacon or area that the device is closest to
    private String previousClosestLocation = null; //This is the previous closest beacon or area which is appended to the user's log
    private boolean firstEntry = true; //Indicates whether this is the first time an exit/entry event has occurred since the app started
    private String setLocation = null; //This location is specifically set in the maps tab and used only when the user is using GPS to determine their location
    private double closestBeaconDist = 100;
    BeaconManager beaconManager;
    private Identifier myBeaconNamespaceId = Identifier.parse("0xd88a52b9f700da5e35f1");

    private ArrayList<LocationTimeStamp> mlocationTimeStamps = new ArrayList<>();
    //LocationTimeStamp lts = new LocationTimeStamp("Testing", getDateCurrentTimeZone(100000), getDurationBreakdown(100000));
    private LinkedHashMap<String, Beacon> beaconList = new LinkedHashMap<>();

    //Alarm Manager
    private int mHours = 9, mMinutes = 0, eHours = 18, eMinutes = 0;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent, alarmIntent2;

    //App ID
    private AppID appID;
    private AppIDAuthorizationManager appIDAuthorizationManager;
    private TokensPersistenceManager tokensPersistenceManager;

    //Location Updates
    private static GpsRequests gps = new GpsRequests();

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        AndroidThreeTen.init(this);
        BeaconManager beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);
//        // By default the AndroidBeaconLibrary will only find AltBeacons.  If you wish to make it
//        // find a different type of beacon, you must specify the byte layout for that beacon's
//        // advertisement with a line like below.  The example shows how to find a beacon with the
//        // same byte layout as AltBeacon but with a beaconTypeCode of 0xaabb.  To find the proper
//        // layout expression for other beacon types, do a web search for "setBeaconLayout"
//        // including the quotes.
//        //
//        //beaconManager.getBeaconParsers().clear();
//        //beaconManager.getBeaconParsers().add(new BeaconParser().
//        //        setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
//
        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        beaconManager.bind(this);
        beaconManager.setDebug(true);
//
//
//        Bluetooth Medic, this is responsible for recovering from a full bluetooth stack, resulting in scan failed errors
//        This programmatically turns the bluetooth of the device on and off
        BluetoothMedic medic = BluetoothMedic.getInstance();
        medic.enablePowerCycleOnFailures(this);
        medic.enablePeriodicTests(this, BluetoothMedic.SCAN_TEST | BluetoothMedic.TRANSMIT_TEST);
//
//
//        // Uncomment the code below to use a foreground service to scan for beacons. This unlocks
//        // the ability to continually scan for long periods of time in the background on Android 8+
//        // in exchange for showing an icon at the top of the screen and a always-on notification to
//        // communicate to users that your app is using resources in the background.
//        //
//    /*
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

            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channel.getId());
        }
        beaconManager.enableForegroundServiceScanning(builder.build(), 456);

        // For the above foreground scanning service to be useful, you need to disable
        // JobScheduler-based scans (used on Android 8+) and set a fast background scan
        // cycle that would otherwise be disallowed by the operating system.
        //
        beaconManager.setEnableScheduledScanJobs(false);
        beaconManager.setBackgroundBetweenScanPeriod(30000);
        beaconManager.setBackgroundScanPeriod(5000);
//*/
        SharedPreferences sharedPreferences = getSharedPreferences("myNamespace", 0);
        Log.d(TAG, "setting up background monitoring for beacons and power saving");
        // wake up the app when a beacon is seen
        if (sharedPreferences.contains("namespace")) {
            myBeaconNamespaceId = Identifier.parse(sharedPreferences.getString("namespace", getString(R.string.mmdt_no_namespace)));
            Log.d(TAG, "myBeaconNamespaceId: " + myBeaconNamespaceId);
        }


        // TODO: Move regionBootstrap to Alarm, regionBootstrap is used to auto-detect beacons
        Region region = new Region("backgroundRegion",
                myBeaconNamespaceId, null, null);
        Log.d(TAG, "Region is: " + region);
        regionBootstrap = new RegionBootstrap(this, region);
//
//        // simply constructing this class and holding a reference to it in your custom Application
//        // class will automatically cause the BeaconLibrary to save battery whenever the application
//        // is not visible.  This reduces bluetooth power usage by about 60%
        backgroundPowerSaver = new BackgroundPowerSaver(this);
//        // set the duration of the scan
//        beaconManager.setBackgroundScanPeriod(30000);
//        // set the time between each scan
//        beaconManager.setBackgroundBetweenScanPeriod(60000);
//        // If you wish to test beacon detection in the Android Emulator, you can use code like this:
//        // BeaconManager.setBeaconSimulator(new TimedBeaconSimulator() );
//        // ((TimedBeaconSimulator) BeaconManager.getBeaconSimulator()).createTimedSimulatedBeacons();

        //Check if alarm is enabled, then set it
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean("alarmEnable", true))
            setAlarm(false);
        else
            cancelAlarm();

        //Load in today's log from the local data store into mLocationTimestamps
        long currentTime = System.currentTimeMillis();
        mlocationTimeStamps = DatabaseSync.getLogs(getDateOnly(currentTime));

        //Initialize AppID
        appID = AppID.getInstance();
        appID.initialize(this, getString(R.string.authTenantId), AppID.REGION_US_SOUTH);
        appIDAuthorizationManager = new AppIDAuthorizationManager(appID);
        tokensPersistenceManager = new TokensPersistenceManager(this, appIDAuthorizationManager);

//        startRangingService();
//        startForeService();

        updateFragment();
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
            Log.d(TAG, "Region is: " + region);
            try {
                beaconManager.stopRangingBeaconsInRegion(region);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            beaconManager.unbind(this);
        }
    }
    public void enableMonitoring() {
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.bind(this);
        SharedPreferences sharedPreferences = getSharedPreferences("myNamespace", 0);
        if (sharedPreferences.contains("namespace")) {
            myBeaconNamespaceId = Identifier.parse(sharedPreferences.getString("namespace", getString(R.string.mmdt_no_namespace)));
        }

        Region region = new Region("backgroundRegion",
                myBeaconNamespaceId, null, null);
        Log.d(TAG, "Region is: " + region);
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
                Log.d(TAG, "Beacon Seen");

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
        time_periodic = System.currentTimeMillis();
        Log.i(TAG, "The Region is" + region.toString());
        Log.i("DidDetermine", "Triggered with state: " + state);

        entryExitAlgorithm(state, region);
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

        if (notificationManager != null)
            notificationManager.notify(1, builder.build());
    }

    public void setMonitoringActivity(MonitoringActivity activity) {
        this.monitoringActivity = activity;
    }

    public void setBeaconSettingActivity(BeaconSettingActivity activity) {
        this.beaconSettingActivity = activity;
    }


    public String getLog() {
        return cumulativeLog;
    }

    public static String getDurationBreakdown(long millis) {
        if(millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }
        Log.i("Duration", "Milliseconds: " + millis);
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
            e.printStackTrace();
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
            Date currentTimeZone = calendar.getTime();
            Log.d("Time: ", sdf.format(currentTimeZone));
            return sdf.format(currentTimeZone);
        }catch (Exception e) {
            e.printStackTrace();
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
        return (now.isAfter(start) && now.isBefore(end));

    }


    public String lastLocation(){
        return  closestLocation;
    }

    public void appendToList(String location, long timestamp,  long millis){
        Log.i(TAG, "appendToList called");
        LocationTimeStamp lts = new LocationTimeStamp(location, getDateCurrentTimeZone(timestamp), getDurationBreakdown(millis));
        mlocationTimeStamps.add(0, lts);
        Log.i(TAG, "List of timestamps: " + mlocationTimeStamps.toString());
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
            editor.apply();
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
                Log.d(TAG, "Region: " +region + "Beacons: " + beacons.size());
                if (beacons.size() > 0) {
                    Log.d(TAG, "didRangeBeaconsInRegion called with beacon count:  "+beacons.size());
                    Beacon firstBeacon = beacons.iterator().next();
                    Log.d(TAG, firstBeacon.toString());
                    //closestAlgorithm(beacons);
                    determineLocation(beacons);
                }
            }

        };
            Log.d(TAG, "Try Ranging");
            beaconManager.removeAllRangeNotifiers();
            beaconManager.addRangeNotifier(rangeNotifier);
        try {
            Region region = new Region("backgroundRegion",
                    myBeaconNamespaceId, null, null);
            beaconManager.startRangingBeaconsInRegion(region);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                 }

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
    public void initNewDay(){
        Log.i(TAG, "Initialize started");
        time_periodic = System.currentTimeMillis();
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
        Log.i(TAG, "Store logs started");
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
                        new storeLogAsync(uri, mlocationTimeStamps, getDateOnly(System.currentTimeMillis())).execute();
                    }
                    Log.i(TAG, "Storelogs: Post Execute Finished");
                }
            }.execute();
        }
    }
    private static class storeLogAsync extends AsyncTask<String, Void, String> {

        private URI uri;
        private ArrayList<LocationTimeStamp> mLocationTimeStamps;
        private String date;

        // only retain a weak reference to the activity
        storeLogAsync(URI uri, ArrayList<LocationTimeStamp> mLocationTimeStamps, String date) {
           this.uri = uri;
           this.mLocationTimeStamps = mLocationTimeStamps;
           this.date = date;
        }
        @Override
        protected String doInBackground(String... strings) {
            DatabaseSync.storeLogs(mLocationTimeStamps, uri, date);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
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

    private void determineLocation(Collection<Beacon> beacons){
        Log.d(TAG, "didRangeBeaconsInRegion called with beacon count:  " + beacons.size());
        //if 9-6
        double rssi = 100;
        LocalTime now = LocalTime.now(ZoneId.systemDefault());
        if (isBetweenWorkHours((now))){

            //Check if the namespace has changed, clear the beaconList if it has
            SharedPreferences sp = getSharedPreferences("myNamespace", 0);
            if (!(myBeaconNamespaceId.toString().equals(sp.getString("namespace", getString(R.string.mmdt_no_namespace))))) {
                Log.i("Namespace", "Is running");
                myBeaconNamespaceId = Identifier.parse(sp.getString("namespace", getString(R.string.mmdt_no_namespace)));
                disableMonitoring();
                enableMonitoring();
                beaconList = new LinkedHashMap<>();
            }

            SharedPreferences sharedPreferences = getSharedPreferences("myBeacons", 0);

            //Determine which beacon in range is the closest
            for (Beacon beacon : beacons) {
                putListOfBeacons(beacon); //Put beacon into the list of beacons in range, used this list when editing beacon info in the beacons setting activity
                Log.i(TAG, "Beacon instance is: "+beacon.getId2().toString());
                //Check if the beacon instance has been saved as a location in shared preferences
                if (sharedPreferences.contains(beacon.getId2().toString())) {
                    String location = sharedPreferences.getString(beacon.getId2().toString(), null);
                    double beaconDist = beacon.getDistance();
                    Log.i(TAG, "I see a beacon " + location + "that is about " + beacon.getDistance() + " meters away.");
                    if (beaconDist < closestBeaconDist) {
                        closestBeaconDist = beaconDist;
                        closestLocation = location;
                        rssi = beacon.getRssi();
                        Log.i(TAG, "The closest beacon is: " + closestLocation + " meters away.");
                    }
                }
            }
            //Update list of beacons
            updateBeaconSettingActivity();
            updateBluetoothFrag(rssi, closestLocation);
            //Determine closest beacon only after pinging beacons for 10 seconds to account for distance measurement errors
            if (time_periodic + 10000 <= System.currentTimeMillis()) {
                closestBeaconDist = 100; //Arbitarily high number
                time_periodic = System.currentTimeMillis();
                long currentTime = System.currentTimeMillis();
                String displayCurrentTime = getDateCurrentTimeZone(currentTime);
                Log.i(TAG, "The closest beacon is: " + closestLocation + " The previous cl is " + previousClosestLocation);
                if (closestLocation != null) {
                    //If Device started inside
                    if (!(closestLocation.equals(previousClosestLocation))) {
                        if (previousClosestLocation == null) {
                            previousClosestLocation = closestLocation;
                            updateCurrentLocationLTS(closestLocation, displayCurrentTime);
                            updateCurrentLocationMaps(closestLocation, displayCurrentTime);
                        } else {
                            //If device has transitioned from beacon to beacon
                            Log.i("didRange", "Beacon to beacon");
                            long duration = currentTime - time_of_entry;
                            //convert current time to date
                            //display time spent and current time
                            appendToList(previousClosestLocation, time_of_entry, duration);
                            Log.i(TAG, "List of timestamps, Beacon->Different Beacon");

                            //Update UI, and Database
                            updateFragment();
                            updateCurrentLocationLTS(closestLocation, displayCurrentTime);
                            updateCurrentLocationMaps(closestLocation, displayCurrentTime);
                            storeLogs(currentTime);

                            time_of_entry = currentTime;
                            Log.i("Time Of Entry" , "5" + time_of_entry);
                            previousClosestLocation = closestLocation;
                        }
                    }
                }
            }
        }
    }

    private void entryExitAlgorithm(int state, Region region){
        //Entry Event
        Log.i("entryExit", "Region is: " + region + "State is " + state);
        BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
        if (state == 1) {
            Log.i("entryExit", "Region is: " + region + "State should be entry " + state);
            try {
                Log.d(TAG, "Region is: " + region);
                beaconManager.startRangingBeaconsInRegion(region);
            }
            catch (RemoteException e) {
                if (BuildConfig.DEBUG) Log.d(TAG, "Can't start ranging");
            }
            LocalTime now = LocalTime.now(ZoneId.systemDefault());

            if (isBetweenWorkHours(now)){
                //For outside of region to inside event

                if (firstEntry){
                    firstEntry = false;
                    time_of_entry = System.currentTimeMillis();
                    Log.i("Time Of Entry" , "4" + time_of_entry);
                } else {
                    previousClosestLocation = closestLocation;
                    Log.i("entryExit", "PCL " + previousClosestLocation);
                }
            }
            gps.stop();

        } else if (state == 0) {
            Log.i("entryExit", "Region is: " + region + "State should be an exit" + state);
            LocalTime now = LocalTime.now(ZoneId.systemDefault());
            if (isBetweenWorkHours((now))) {

                if (firstEntry){
                    firstEntry = false;
                    time_of_entry = System.currentTimeMillis();
                    Log.i("Time Of Entry" , "1" + time_of_entry);
                }

                if (closestLocation != null) {
                    //Inside to outside
                    Log.i("DidDetermine", "Inside to Outside");
                    if (previousClosestLocation != null) {
                        if (!previousClosestLocation.equals("Outside")) {
                            long duration = System.currentTimeMillis() - time_of_entry;
                            appendToList(previousClosestLocation, time_of_entry, duration);
                            updateFragment();
                            storeLogs(time_of_entry);
                        }
                    }
                    //Set time of entry and store previous closest location
                    previousClosestLocation = closestLocation;
                }

                if (previousClosestLocation != null) {
                    if (!previousClosestLocation.equals("Outside"))
                        time_of_entry = System.currentTimeMillis();
                    Log.i("Time Of Entry" , "2" + time_of_entry);
                } else {
                    time_of_entry = System.currentTimeMillis();
                    Log.i("Time Of Entry", "3" + time_of_entry);
                }

                String displayCurrentTime = getDateCurrentTimeZone(time_of_entry);
                closestLocation = "Outside";
                updateBluetoothFrag(0, closestLocation);
                updateCurrentLocationLTS(closestLocation, displayCurrentTime);
                updateCurrentLocationMaps(closestLocation, displayCurrentTime);

                // TODO: ACQUIRE GPS
                gps.start();
            }
        }
    }

    public long latestTimeStamp(){
        return time_of_entry;
    }

    private void updateBluetoothFrag(double dist, String location){
        Intent intent1 = new Intent();
        intent1.setAction("update_bluetooth_fragment");
        intent1.putExtra("dist", dist);
        intent1.putExtra("location", location);
        sendBroadcast(intent1);
        Log.i("UpdateUI", "Sent broadcast");
    }

    public void stopGPS(){
        gps.stop();
    }

}





