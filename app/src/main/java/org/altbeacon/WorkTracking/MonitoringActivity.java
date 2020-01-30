package org.altbeacon.WorkTracking;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;


//Materials Design
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;

/**
 */
public class MonitoringActivity extends AppCompatActivity implements BeaconConsumer{
	protected static final String TAG = "MonitoringActivity";
	private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
	private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;

	BeaconManager beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);
	TextView namespaceID;
	TextView instanceID;
	TextView locationID;
	TextView timeID1, timeID2;
	SharedPreferences sharedpreferences;
	InputFilter timeFilter;
	private boolean doneOnce = false;

	//Shared Preferences
	public static final String myPreference = "myBeacons";
	Identifier myBeaconNamespaceId = Identifier.parse("0x00112233445566778898");

	//Alarm Manager
	private int mHours = 9, mMinutes = 0, eHours = 18, eMinutes = 0;
	private AlarmManager alarmMgr;
	private PendingIntent alarmIntent, alarmIntent2;

	//Layout
	private Toolbar toolbar;
	private TabLayout tabLayout;
	private ViewPager viewPager;
	private int[] tabIcons = {
			R.drawable.logicon,
			R.drawable.ic_map_black_24dp,
			R.drawable.ic_settings_black_24dp
	};
	ProfileDrawerItem profile;
	AccountHeader headerResult;

	//Firebase
	private FirebaseAuth.AuthStateListener mAuthListener;
	private FirebaseAuth mAuth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monitoring);
		verifyBluetooth();

//		if (savedInstanceState == null) {
//			getSupportFragmentManager()
//					.beginTransaction()
//					.add(R.id.container, new org.altbeacon.beaconreference.LocationTimestampFragment(), "LTS_FRAGMENT")
//					.commit();
//		}
		//Setup Toolbar
		toolbar = (Toolbar) findViewById(R.id.app_bar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		//Setup Nav Drawer
		PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName("Home").withIcon(R.drawable.nav_ic_home_black_24dp);
		SecondaryDrawerItem item2 = new SecondaryDrawerItem().withIdentifier(2).withName("My Profile").withIcon(R.drawable.nav_ic_person_outline_black_24dp);;
		SecondaryDrawerItem item3 = new SecondaryDrawerItem().withIdentifier(3).withName("Beacons").withIcon(R.drawable.nav_ic_bluetooth_searching_black_24dp);
		SecondaryDrawerItem item4 = new SecondaryDrawerItem().withIdentifier(4).withName("Calendar").withIcon(R.drawable.nav_ic_calendar_today_24px);
		SecondaryDrawerItem item5 = new SecondaryDrawerItem().withIdentifier(5).withName("Logout").withIcon(R.drawable.nav_ic_exit_to_app_black_24dp);;

		// Create the AccountHeader for the Nav Drawer
		sharedpreferences = getSharedPreferences("myProfile", 0);
		String navDrawHeaderName = sharedpreferences.getString("profileName", "Set your name in your profile page");
		String navDrawHeaderEmail = sharedpreferences.getString("profileEmail", "Set your email in your profile page");

		profile = new ProfileDrawerItem().withName(navDrawHeaderName).withEmail(navDrawHeaderEmail)
				.withIcon(getResources().getDrawable(R.drawable.employee));

		headerResult = new AccountHeaderBuilder()
				.withActivity(this)
				.withHeaderBackground(R.drawable.header)
				.addProfiles(profile)
				.withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
					@Override
					public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
						return false;
					}
				})
				.build();

		//create the drawer and remember the `Drawer` result object
		Drawer result = new DrawerBuilder()
				.withActivity(this)
				.withToolbar(toolbar)
				.withAccountHeader(headerResult)
				.addDrawerItems(
						item1,
						new DividerDrawerItem(),
						item2,
						item3,
						item4,
						new DividerDrawerItem(),
						item5
				)
				.withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
					@Override
					public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
						switch ((int) drawerItem.getIdentifier()) {
							case 2:
								startActivity(new Intent(MonitoringActivity.this, ProfileActivity.class));
								break;
							case 3:
								startActivity(new Intent(MonitoringActivity.this, BeaconSettingActivity.class));
								break;
							case 4:
								startActivity(new Intent(MonitoringActivity.this, CalendarActivity.class));
								break;
							case 5:
								FirebaseAuth.getInstance().signOut();
								FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
								Log.i("Firebase", "Check if user is null: " + user);
								startActivity(new Intent(MonitoringActivity.this, MainActivity.class));
								break;
							default:
								return true;
						}
						return false;
					}
				})
				.build();
		// Get the ViewPager and set it's PagerAdapter so that it can display items
		viewPager = (ViewPager) findViewById(R.id.view_pager);
		viewPager.setAdapter(new TabPagerAdapter(getSupportFragmentManager(),
				MonitoringActivity.this));

		// Give the TabLayout the ViewPager
		tabLayout = (TabLayout) findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(viewPager);
		setupViewPager(viewPager);
		setupTabIcons();


		//Firebase sign out observer
		mAuthListener = new FirebaseAuth.AuthStateListener() {
			@Override
			public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
				FirebaseUser user = firebaseAuth.getCurrentUser();
				if (user != null) {
					// User is signed in
					Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
				} else {
					// User is signed out
					Log.d(TAG, "onAuthStateChanged:signed_out");
				}
				// ...
			}
		};

		//Bind The beaconmanager,
		beaconManager.bind(this);
		beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
					== PackageManager.PERMISSION_GRANTED) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
					if (this.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
							!= PackageManager.PERMISSION_GRANTED) {
						if (!this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
							final AlertDialog.Builder builder = new AlertDialog.Builder(this);
							builder.setTitle("This app needs background location access");
							builder.setMessage("Please grant location access so this app can detect beacons in the background.");
							builder.setPositiveButton(android.R.string.ok, null);
							builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

								@TargetApi(23)
								@Override
								public void onDismiss(DialogInterface dialog) {
									requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
											PERMISSION_REQUEST_BACKGROUND_LOCATION);
								}

							});
							builder.show();
						} else {
							final AlertDialog.Builder builder = new AlertDialog.Builder(this);
							builder.setTitle("Functionality limited");
							builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons in the background.  Please go to Settings -> Applications -> Permissions and grant background location access to this app.");
							builder.setPositiveButton(android.R.string.ok, null);
							builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

								@Override
								public void onDismiss(DialogInterface dialog) {
								}

							});
							builder.show();
						}
					}
				}
			} else {
				if (!this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
					requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
									Manifest.permission.ACCESS_BACKGROUND_LOCATION},
							PERMISSION_REQUEST_FINE_LOCATION);
				} else {
					final AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("Functionality limited");
					builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.  Please go to Settings -> Applications -> Permissions and grant location access to this app.");
					builder.setPositiveButton(android.R.string.ok, null);
					builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

						@Override
						public void onDismiss(DialogInterface dialog) {
						}

					});
					builder.show();
				}

			}
		}

		//Setup Alarm Manager and broadcaster to wake beacon up and sleep beacon services at specified times
		alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(this, AlarmReceiver.class);
		alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
		Intent intent2 = new Intent(this, AlarmReceiver.class);
		alarmIntent2 = PendingIntent.getBroadcast(this, 0, intent2, 0);

		Calendar scheduleCalendar = Calendar.getInstance();
		scheduleCalendar.setTimeInMillis(System.currentTimeMillis());
		Calendar scheduleCalendar2 = Calendar.getInstance();
		scheduleCalendar2.setTimeInMillis(System.currentTimeMillis());

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

		scheduleCalendar.set(Calendar.HOUR_OF_DAY, mMinutes);
		scheduleCalendar.set(Calendar.MINUTE, mHours);
		scheduleCalendar2.set(Calendar.HOUR_OF_DAY, eMinutes);
		scheduleCalendar2.set(Calendar.MINUTE, eHours);

		alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, scheduleCalendar.getTimeInMillis(),
				AlarmManager.INTERVAL_DAY, alarmIntent);
		alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, scheduleCalendar2.getTimeInMillis(),
				AlarmManager.INTERVAL_DAY, alarmIntent2);

	}



	@Override
	protected void onDestroy() {
		super.onDestroy();
		beaconManager.unbind(this);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		switch (requestCode) {
			case PERMISSION_REQUEST_FINE_LOCATION: {
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					Log.d(TAG, "fine location permission granted");
				} else {
					final AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("Functionality limited");
					builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons.");
					builder.setPositiveButton(android.R.string.ok, null);
					builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

						@Override
						public void onDismiss(DialogInterface dialog) {
						}

					});
					builder.show();
				}
				return;
			}
			case PERMISSION_REQUEST_BACKGROUND_LOCATION: {
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					Log.d(TAG, "background location permission granted");
				} else {
					final AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle("Functionality limited");
					builder.setMessage("Since background location access has not been granted, this app will not be able to discover beacons when in the background.");
					builder.setPositiveButton(android.R.string.ok, null);
					builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

						@Override
						public void onDismiss(DialogInterface dialog) {
						}

					});
					builder.show();
				}
				return;
			}
		}
	}

    @Override
    public void onResume() {
        super.onResume();
        MainApplication application = ((MainApplication) this.getApplicationContext());
        application.setMonitoringActivity(this);
		headerResult.updateProfile(profile);
        //updateLog(application.getLog());
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainApplication) this.getApplicationContext()).setMonitoringActivity(null);
    }

	private void verifyBluetooth() {

		try {
			if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Bluetooth not enabled");
				builder.setMessage("Please enable bluetooth in settings and restart this application.");
				builder.setPositiveButton(android.R.string.ok, null);
				builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						//finish();
			            //System.exit(0);
					}
				});
				builder.show();
			}
		}
		catch (RuntimeException e) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Bluetooth LE not available");
			builder.setMessage("Sorry, this device does not support Bluetooth LE.");
			builder.setPositiveButton(android.R.string.ok, null);
			builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					//finish();
		            //System.exit(0);
				}

			});
			builder.show();

		}

	}

//    public void updateLog(final String log) {
//    	runOnUiThread(new Runnable() {
//    	    public void run() {
//    	    	EditText editText = (EditText)MonitoringActivity.this
//    					.findViewById(R.id.monitoringText);
//       	    	editText.setText(log);
//    	    }
//    	});
//    }

	@Override
	public void onBeaconServiceConnect() {

		try {
			SharedPreferences sharedPreferences = getSharedPreferences("myBeacons", 0);
			if (sharedPreferences.contains("namespace")) {
				myBeaconNamespaceId = Identifier.parse(sharedPreferences.getString("namespace", null));
			}
			Region region = new Region("backgroundRegion",
					myBeaconNamespaceId, null, null);

			beaconManager.startMonitoringBeaconsInRegion(region);
		} catch (RemoteException e) {    }
	}

	public boolean isFragmentVisible(String fragName){
		Log.d(TAG, "isFragmentVisible called");
		TabPagerAdapter adapter = (TabPagerAdapter) viewPager.getAdapter();
		Fragment myFragment = adapter.returnFragment(fragName);
		return (myFragment != null && myFragment.isVisible());
	}

	public LocationTimestampFragment returnLtsFragment(){
		TabPagerAdapter adapter = (TabPagerAdapter) viewPager.getAdapter();
		LocationTimestampFragment myFragment = (LocationTimestampFragment) adapter.returnFragment("LTS_FRAG");
		return myFragment;
	}

	public MapsFragment returnMapsFragment(){
		TabPagerAdapter adapter = (TabPagerAdapter) viewPager.getAdapter();
		MapsFragment myFragment = (MapsFragment) adapter.returnFragment("MAPS_FRAG");
		return myFragment;
	}

	private void setupViewPager(ViewPager viewPager) {
		TabPagerAdapter adapter = (TabPagerAdapter) viewPager.getAdapter();
		//adapter.addFrag(new LocationTimestampFragment(), "LTS_FRAG");
		//adapter.addFrag(new MapsFragment(), "MAPS_FRAG");
		//adapter.addFrag(new MapsFragment(), "SETTINGS_FRAG");
		viewPager.setAdapter(adapter);
	}

	private void setupTabIcons() {
		tabLayout.getTabAt(0).setIcon(tabIcons[0]);
		tabLayout.getTabAt(1).setIcon(tabIcons[1]);
		tabLayout.getTabAt(2).setIcon(tabIcons[2]);
	}

	@Override
	protected void onStart() {
		super.onStart();
		headerResult.updateProfile(profile);
	}


}
