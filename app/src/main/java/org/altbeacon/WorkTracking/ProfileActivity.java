package org.altbeacon.WorkTracking;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ibm.cloud.appid.android.api.AppID;
import com.ibm.cloud.appid.android.api.AppIDAuthorizationManager;
import com.ibm.cloud.appid.android.api.userprofile.UserProfileException;
import com.ibm.cloud.appid.android.api.userprofile.UserProfileResponseListener;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    static final int TEXT_INPUT = 0;
    static final int PHONE_INPUT = 1;
    final String TAG = "ProfileActivity";
    //AppId Profile
    private AppID appID;
    private AppIDAuthorizationManager appIDAuthorizationManager;

    //Views
    ImageView profileNameIcon, profileEmailIcon, profilePhoneIcon, profileJobIcon;
    TextView profileName, profileEmail, profilePhone, profileJob;
    SharedPreferences sp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Set Title
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        collapsingToolbarLayout.setTitleEnabled(true);
        collapsingToolbarLayout.setTitle("My Profile");

        //TextViews
        profileName = findViewById(R.id.profile_name);
        profileEmail = findViewById(R.id.profile_email);
        profilePhone = findViewById(R.id.profile_phone);
        profileJob = findViewById(R.id.profile_job);

        //Edit Buttons
        profileNameIcon = findViewById(R.id.profile_name_edit_icon);
        profileNameIcon.setOnClickListener(this);
        profileEmailIcon = findViewById(R.id.profile_email_edit_icon);
        profileEmailIcon.setOnClickListener(this);
        profilePhoneIcon = findViewById(R.id.profile_phone_edit_icon);
        profilePhoneIcon.setOnClickListener(this);
        profileJobIcon = findViewById(R.id.profile_jobtitle_edit_icon);
        profileJobIcon.setOnClickListener(this);

        //Load details from shared preferences
        sp = getSharedPreferences("myProfile", 0);
        profileName.setText(sp.getString("profileName", "Enter Your Name"));
        profileEmail.setText(sp.getString("profileEmail", "Enter Your Email"));
        profilePhone.setText(sp.getString("profilePhone", "Enter Your Phone"));
        profileJob.setText(sp.getString("profileJob", "Enter Your Job Title"));

        //AppId Init
        appID = AppID.getInstance();
        appIDAuthorizationManager = new AppIDAuthorizationManager(appID);
    }


    void alertDialog(String titleString, int inputType, int detailType) {
        // get alert_dialog.xml view
        LayoutInflater li = LayoutInflater.from(getApplicationContext());
        View promptsView = li.inflate(R.layout.alert_dialog, null);


        MaterialAlertDialogBuilder alertDialogBuilder = new MaterialAlertDialogBuilder(
                ProfileActivity.this, R.style.myDialog);

        // set alert_dialog.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);



        final EditText userInput = (EditText) promptsView.findViewById(R.id.etUserInput);
        final TextView title = promptsView.findViewById(R.id.alertTitle);
        title.setText(titleString);
        if (inputType == PHONE_INPUT)
            userInput.setInputType(InputType.TYPE_CLASS_PHONE);
        if (inputType == TEXT_INPUT)
            userInput.setInputType(InputType.TYPE_CLASS_TEXT);

        sp = getSharedPreferences("myProfile", 0);
        SharedPreferences.Editor editor = sp.edit();
        Log.i("Button Test", "Alert Dialog called");
        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // get user input and set it to result
                        // edit text
                        switch (detailType){
                            case 1:
                                appID.getUserProfileManager().setAttribute("displayName", userInput.getText().toString(), new UserProfileResponseListener() {
                                    @Override
                                    public void onSuccess(JSONObject attributes) {
                                        // Set attribute "name" to "value" successfully
                                        Log.i(TAG, "Set Name Successful");
                                        editor.putString("profileName", userInput.getText().toString());
                                        editor.commit();
                                    }

                                    @Override
                                    public void onFailure(UserProfileException e) {
                                        // Exception occurred
                                        //Failure Dialog
                                    }
                                });
                                String name = sp.getString("profileName", "No name given");
                                profileName.setText(name);
                                break;

                            case 2:
                                appID.getUserProfileManager().setAttribute("email", userInput.getText().toString(), new UserProfileResponseListener() {
                                    @Override
                                    public void onSuccess(JSONObject attributes) {
                                        // Set attribute "name" to "value" successfully
                                        editor.putString("profileEmail", userInput.getText().toString());
                                        editor.commit();
                                    }

                                    @Override
                                    public void onFailure(UserProfileException e) {
                                        // Exception occurred
                                        //Failure Dialog
                                    }
                                });
                                String email = sp.getString("profileEmail", "No name given");
                                profileEmail.setText(email);
                                break;

                            case 3:
                                editor.putString("profilePhone", userInput.getText().toString());
                                editor.commit();
                                profilePhone.setText(userInput.getText().toString());
                                break;

                            case 4:
                                editor.putString("profileJob", userInput.getText().toString());
                                editor.commit();
                                profileJob.setText(userInput.getText().toString());
                                break;
                        }
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


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.profile_name_edit_icon) {
            Log.i("Button Test", "Name Button called");
            alertDialog("Enter your name", TEXT_INPUT, 1);
        } else if (i == R.id.profile_email_edit_icon) {
            Log.i("Button Test", "Email Button called");
            alertDialog("Enter your email address", TEXT_INPUT, 2);
        } else if (i == R.id.profile_phone_edit_icon) {
            alertDialog("Enter your phone number", PHONE_INPUT, 3);
        } else if (i == R.id.profile_jobtitle_edit_icon) {
            alertDialog("Enter your job description", TEXT_INPUT, 4);
        }
    }


}
