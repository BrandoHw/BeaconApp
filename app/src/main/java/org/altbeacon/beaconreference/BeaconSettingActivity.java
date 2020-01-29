package org.altbeacon.beaconreference;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.w3c.dom.Text;

import java.util.ArrayList;

public class BeaconSettingActivity extends AppCompatActivity implements BeaconSettingsRecycleViewAdapter.OnCardClickListener{
    String TAG = "BeaconSettingActivity";
    BeaconSettingsRecycleViewAdapter mAdapter;
    private ArrayList<Beacon> mBeaconArray = new ArrayList<Beacon>();
    TextView namespace;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Beacon Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        BeaconReferenceApplication application = ((BeaconReferenceApplication) getApplicationContext());
        // Inflate the layout for this fragment with the ProductGrid theme

        // Set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recycler_view_beacons);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new BeaconSettingsRecycleViewAdapter(
                application.returnBeaconList(), this, this);
        recyclerView.setAdapter(mAdapter);

        //Set up onclick listener for namespace header
        LinearLayout namespace_header = findViewById(R.id.namespace_header);
        namespace = findViewById(R.id.namespace);
        SharedPreferences sp = getSharedPreferences("myNamespace", 0);
        String namespace_id = sp.getString("namespace", getString(R.string.mmdt_no_namespace));
        namespace.setText("Namespace: " + namespace_id);
        namespace_header.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                alertDialog2();
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_beacon_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.beacon_settings_delete:
                alertDialog3();
                return true;
            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        BeaconReferenceApplication application = ((BeaconReferenceApplication) this.getApplicationContext());
        application.setBeaconSettingActivity(this);
        //updateLog(application.getLog());
    }

    @Override
    public void onPause() {
        super.onPause();
        ((BeaconReferenceApplication) this.getApplicationContext()).setBeaconSettingActivity(null);
    }

    public void updateBeaconList(){
        BeaconReferenceApplication application = ((BeaconReferenceApplication) getApplicationContext());
        //Log.i(TAG, "Attempting adapter update");
        if (!application.returnBeaconList().isEmpty()) {
            //Log.i(TAG, "Successful adapter update");
            Log.i(TAG, "The size is: " + application.returnBeaconList().size());
            mBeaconArray = mAdapter.update(application.returnBeaconList());
        }
    }

    //Alert dialog to set beacon locations
    void alertDialog(String instanceId) {
        // get alert_dialog.xml view
        LayoutInflater li = LayoutInflater.from(getApplicationContext());
        View promptsView = li.inflate(R.layout.alert_dialog, null);


        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(
                BeaconSettingActivity.this, R.style.myDialog);

        // set alert_dialog.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView.findViewById(R.id.etUserInput);
        final TextView title = promptsView.findViewById(R.id.alertTitle);
        title.setText("Enter the name of the location: ");

        SharedPreferences sp;
        sp = getSharedPreferences("myBeacons", 0);
        SharedPreferences.Editor editor = sp.edit();
        Log.i("Button Test", "Alert Dialog called");
        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // get user input and set it to result
                        // edit text
                        editor.putString(instanceId, userInput.getText().toString());
                        editor.commit();
                        updateBeaconList();
                        Toast.makeText(getApplicationContext(), "Entered: "+userInput.getText().toString(), Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    //Alert dialog to set namespace
    void alertDialog2() {
        // get alert_dialog.xml view
        LayoutInflater li = LayoutInflater.from(getApplicationContext());
        View promptsView = li.inflate(R.layout.alert_dialog, null);


        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(
                BeaconSettingActivity.this, R.style.myDialog);

        // set alert_dialog.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView.findViewById(R.id.etUserInput);
        final TextView title = promptsView.findViewById(R.id.alertTitle);
        title.setText("Enter the new namespace: ");

        SharedPreferences sp;
        sp = getSharedPreferences("myNamespace", 0);
        SharedPreferences.Editor editor = sp.edit();
        Log.i("Button Test", "Alert Dialog called");
        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // get user input and set it to result
                        // edit text
                        editor.putString("namespace", userInput.getText().toString());
                        editor.commit();
                        namespace.setText("Namespace: " + userInput.getText().toString());
                        Toast.makeText(getApplicationContext(), "Entered: "+userInput.getText().toString(), Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    //Alert dialog to delete all beacon locations
    void alertDialog3() {
        // get alert_dialog.xml view
        LayoutInflater li = LayoutInflater.from(getApplicationContext());
        View promptsView = li.inflate(R.layout.alert_dialog_confirm, null);


        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(
                BeaconSettingActivity.this, R.style.myDialog);

        // set alert_dialog.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final TextView title = promptsView.findViewById(R.id.alertTitle);
        title.setText("Are you sure you want to delete all beacons?");

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        clearAll();
                        Toast.makeText(getApplicationContext(), "All beacons deleted", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }


    @Override
    public void onCardClick(int position) {
        Beacon mBeacon = mBeaconArray.get(position);
        alertDialog(mBeacon.getId2().toString());
    }

    public void clearAll(){
        SharedPreferences sp, sp2, sp3;
        //Remove The list of beacons, the list of pinned beacons, and the list of pins
        sp = getSharedPreferences("myBeacons", 0);
        sp2 = getSharedPreferences("myPinnedBeacons", 0);
        sp3 = getSharedPreferences("myPins", 0);
        SharedPreferences.Editor editor = sp.edit();
        SharedPreferences.Editor editor2 = sp2.edit();
        SharedPreferences.Editor editor3 = sp3.edit();
        editor.clear();
        editor.commit();
        editor2.clear();
        editor2.commit();
        editor3.clear();
        editor3.commit();
    }
}
