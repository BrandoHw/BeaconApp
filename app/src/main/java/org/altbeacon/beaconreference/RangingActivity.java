package org.altbeacon.beaconreference;

import java.util.ArrayList;
import java.util.Collection;

import android.app.Activity;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.EditText;

import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import org.altbeacon.beacon.AltBeacon;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

public class RangingActivity extends AppCompatActivity implements BeaconConsumer {
    protected static final String TAG = "RangingActivity";
    private BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranging);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        beaconManager.unbind(this);
    }

    @Override 
    protected void onResume() {
        super.onResume();
        beaconManager.bind(this);
    }

    @Override
    public void onBeaconServiceConnect() {

        RangeNotifier rangeNotifier = new RangeNotifier() {
           @Override
           public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
              if (beacons.size() > 0) {
                  Log.d(TAG, "didRangeBeaconsInRegion called with beacon count:  "+beacons.size());
                  Beacon firstBeacon = beacons.iterator().next();
                  logToDisplay("The first beacon " + firstBeacon.toString() + " is about " + firstBeacon.getDistance() + " meters away.");
              }
           }

        };
        Identifier myBeaconNamespaceId = Identifier.parse("0x00112233445566778899");
        Identifier myBeaconInstanceId = Identifier.parse("0xabcde090000b");
        Region region = new Region("my-beacon-region", myBeaconNamespaceId, null, null);
        try {
            beaconManager.startRangingBeaconsInRegion(region);
            beaconManager.addRangeNotifier(rangeNotifier);
            beaconManager.startRangingBeaconsInRegion(region);
            beaconManager.addRangeNotifier(rangeNotifier);
        } catch (RemoteException e) {   }
    }

    private void logToDisplay(final String line) {
        runOnUiThread(new Runnable() {
            public void run() {
                EditText editText = (EditText)RangingActivity.this.findViewById(R.id.rangingText);
                editText.append(line+"\n");
            }
        });
    }
}
