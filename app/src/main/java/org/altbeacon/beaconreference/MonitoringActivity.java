package org.altbeacon.beaconreference;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AppComponentFactory;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DateFormat;
import java.util.Collection;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.*;


import java.util.HashMap;
import java.util.Map;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

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

	public static final String myPreference = "myBeacons";
	Identifier myBeaconNamespaceId = Identifier.parse("0x00112233445566778898");

	private int mHours = 9, mMinutes = 0, eHours = 18, eMinutes = 0;
	private AlarmManager alarmMgr;
	private PendingIntent alarmIntent, alarmIntent2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monitoring);
		verifyBluetooth();


		namespaceID = (TextView) findViewById(R.id.editTextName);
		instanceID = (TextView) findViewById(R.id.editTextInstance);
		locationID = (TextView) findViewById(R.id.editTextLocation);
		timeID1 = (TextView) findViewById(R.id.editTextMorning);
		timeID2 = (TextView) findViewById(R.id.editTextEvening);

		sharedpreferences = getSharedPreferences(myPreference,
				Context.MODE_PRIVATE);

		beaconManager.bind(this);
		beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));

		timeFilter = new InputFilter() {
			@Override
			public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

				if (source.length() > 1 && doneOnce == false) {
					source = source.subSequence(source.length() - 1, source.length());
					if (source.charAt(0) >= '0' && source.charAt(0) <= '2') {
						doneOnce = true;
						return source;
					} else {
						return "";
					}
				}


				if (source.length() == 0) {
					return null;// deleting, keep original editing
				}
				String result = "";
				result += dest.toString().substring(0, dstart);
				result += source.toString().substring(start, end);
				result += dest.toString().substring(dend, dest.length());

				if (result.length() > 5) {
					return "";// do not allow this edit
				}
				boolean allowEdit = true;
				char c;
				if (result.length() > 0) {
					c = result.charAt(0);
					allowEdit &= (c >= '0' && c <= '2');
				}
				if (result.length() > 1) {
					c = result.charAt(1);
					if (result.charAt(0) == '0' || result.charAt(0) == '1')
						allowEdit &= (c >= '0' && c <= '9');
					else
						allowEdit &= (c >= '0' && c <= '3');
				}
				if (result.length() > 2) {
					c = result.charAt(2);
					allowEdit &= (c == ':');
				}
				if (result.length() > 3) {
					c = result.charAt(3);
					allowEdit &= (c >= '0' && c <= '5');
				}
				if (result.length() > 4) {
					c = result.charAt(4);
					allowEdit &= (c >= '0' && c <= '9');
				}
				return allowEdit ? null : "";
			}

		};

		timeID1.setFilters(new InputFilter[]{timeFilter});
		timeID2.setFilters(new InputFilter[]{timeFilter});
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

		SharedPreferences sharedPreferences = getSharedPreferences("myBeacons", 0);
		if (sharedPreferences.contains("morningHours")) {
			mHours = Integer.parseInt(sharedPreferences.getString("morningHours", null));
			Log.i(TAG, "Set Time: " + Integer.toString(mHours));
		}
		if (sharedPreferences.contains("morningMinutes")) {
			mMinutes = Integer.parseInt(sharedPreferences.getString("morningMinutes", null));
			Log.i(TAG, "Set Time: " + Integer.toString(mMinutes));
		}
		if (sharedPreferences.contains("eveningHours")) {
			eHours = Integer.parseInt(sharedPreferences.getString("eveningHours", null));
			Log.i(TAG, "Set Time: " + Integer.toString(eHours));
		}
		if (sharedPreferences.contains("eveningMinutes")) {
			eMinutes = Integer.parseInt(sharedPreferences.getString("eveningMinutes", null));
			Log.i(TAG, "Set Time: "+ Integer.toString(eMinutes));
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
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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

	public void onRangingClicked(View view) {
		Intent myIntent = new Intent(this,RangingActivity.class);
		this.startActivity(myIntent);
	}
	public void onEnableClicked(View view) {
		BeaconReferenceApplication application = ((BeaconReferenceApplication) this.getApplicationContext());
		if (BeaconManager.getInstanceForApplication(this).getMonitoredRegions().size() > 0) {
			application.disableMonitoring();
			((Button)findViewById(R.id.enableButton)).setText("Re-Enable Monitoring");
		}
		else {
			((Button)findViewById(R.id.enableButton)).setText("Disable Monitoring");
			application.enableMonitoring();
		}

	}

	public void onAddClicked(View view) {
		String n = namespaceID.getText().toString();
		String i = instanceID.getText().toString();
		String l = locationID.getText().toString();
		SharedPreferences.Editor editor = sharedpreferences.edit();
		editor.putString("namespace", n);
		editor.putString(i, l);
		editor.commit();
		Toast toast = Toast.makeText(this, "Beacon Added", Toast.LENGTH_LONG);
		toast.show();
	}
	public void onRemoveClicked(View view) {
		String n = namespaceID.getText().toString();
		String i = instanceID.getText().toString();
		SharedPreferences.Editor editor = sharedpreferences.edit();
		editor.remove("namespace").commit();
		editor.remove(i).commit();
		Toast toast = Toast.makeText(this, "Beacon Removed", Toast.LENGTH_LONG);
		toast.show();
	}

	public void onAddScheduleClicked(View view) {
		String morning = timeID1.getText().toString();
		String evening = timeID2.getText().toString();
		String hours1 = "9";
		String hours2 = "18";
		String minutes1 = "0";
		String minutes2 = "0";
		if (morning.length() > 4)
		{
			hours1 = morning.substring(0, 2);
			minutes1 = morning.substring(morning.length() - 2);
		}
		if (evening.length() > 4)
		{
			hours2 = evening.substring(0, 2);
			minutes2 = evening.substring(evening.length() - 2);
		}

		SharedPreferences.Editor editor = sharedpreferences.edit();
		editor.putString("morningHours", hours1);
		editor.putString("morningMinutes", minutes1);
		editor.putString("eveningHours", hours2);
		editor.putString("eveningMinutes", minutes2);
		editor.commit();
		Toast toast = Toast.makeText(this, "Schedule Changed", Toast.LENGTH_LONG);
		toast.show();

	}

    @Override
    public void onResume() {
        super.onResume();
        BeaconReferenceApplication application = ((BeaconReferenceApplication) this.getApplicationContext());
        application.setMonitoringActivity(this);
        updateLog(application.getLog());
    }

    @Override
    public void onPause() {
        super.onPause();
        ((BeaconReferenceApplication) this.getApplicationContext()).setMonitoringActivity(null);
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

    public void updateLog(final String log) {
    	runOnUiThread(new Runnable() {
    	    public void run() {
    	    	EditText editText = (EditText)MonitoringActivity.this
    					.findViewById(R.id.monitoringText);
       	    	editText.setText(log);
    	    }
    	});
    }

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
}
