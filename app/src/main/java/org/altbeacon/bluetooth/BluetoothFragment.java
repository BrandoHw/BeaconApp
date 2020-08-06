package org.altbeacon.bluetooth;

import android.annotation.TargetApi;
import android.app.Dialog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.biometrics.BiometricPrompt;

import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.eyalbira.loadingdots.LoadingDots;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.android.material.button.MaterialButton;

import org.altbeacon.WorkTracking.R;
import org.altbeacon.biometrics.BiometricCallback;
import org.altbeacon.biometrics.FingerprintHandler;
import org.altbeacon.biometrics.FingerprintUtils;
import org.altbeacon.utils.DateStringUtils;
import java.util.List;


public class BluetoothFragment extends Fragment{

    private static final String TAG = "BluetoothFrag";
    private static final int STATE_NOT_AVAILABLE= 0;
    private static final int STATE_CHECK_IN = 1;
    private static final int STATE_CHECK_OUT = 2;

    private int state = 0;
    private String location = "Outside";
    private Dialog dialog, veri_dialog;
    private TextView textView, status_text;
    private ImageView bluetooth_image;
    private MaterialButton button;
    private CircularProgressView progressBar;
    private LoadingDots loadingDots;
    private FingerprintUtils fingerprintUtils;
    private int verified_state = 0;


    private AttendanceViewModel mAttendanceViewModel;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double s1 = intent.getDoubleExtra("dist", 100);
            String s2 = intent.getStringExtra("location");


            if (!location.equals(s2)) {
                if (s1 == 100)
                    status_text.setText(getString(R.string.location, s2));
                else
                    status_text.setText(getString(R.string.location_rssi, s2, s1));
                location = s2;

                if (location == null)
                    button.setEnabled(false);
                else if (location.equals("Outside"))
                    button.setEnabled(false);
                else
                    button.setEnabled(true);

                mAttendanceViewModel.refresh(location);
                Log.i(TAG, "Update UI: " + location);
            }
        }
    };

    public static BluetoothFragment newInstance() {
        return new BluetoothFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.bluetooth_fragment, container, false);
        textView = view.findViewById(R.id.boldText);
        status_text = view.findViewById(R.id.status_text);
        button = view.findViewById(R.id.button1);
        bluetooth_image = view.findViewById(R.id.bluetooth_icon);
        progressBar = view.findViewById(R.id.progressBar);
        loadingDots = view.findViewById(R.id.loadingDots);

        button.setEnabled(false);
        status_text.setText(getString(R.string.location, "Outside"));

        mAttendanceViewModel = new ViewModelProvider(getActivity()).get(AttendanceViewModel.class);

        mAttendanceViewModel.refresh(location);

        mAttendanceViewModel.getRecordsTodayForLocation().observe(getActivity(), new Observer<List<AttendanceRecord>>() {
            @Override
            public void onChanged(List<AttendanceRecord> attendanceRecords) {
                Log.i(TAG, "List of refreshed records: " + attendanceRecords);

                if (location.equals("Outside")) {
                    setState(STATE_NOT_AVAILABLE);
                } else if (!attendanceRecords.isEmpty()){
                    for (AttendanceRecord aR : attendanceRecords) {
                        Log.i(TAG, "Location: " + aR.getLocation() +
                                " Check in: " + aR.getCheck_in() +
                                " Status " + aR.getStatus());
                        if (aR.getCheck_out() != null)
                            Log.i(TAG, "Check out: " + aR.getCheck_out());
                        if (aR.getLocation().equals(location) && aR.getStatus() == 1) {
                            setState(STATE_CHECK_OUT);
                            break;
                        } else {
                            setState(STATE_CHECK_IN);
                        }
                    }
                } else {
                    setState(STATE_CHECK_IN);
                }
            }
        });

        mAttendanceViewModel.getRecordsToday().observe(getActivity(), new Observer<List<AttendanceRecord>>() {
            @Override
            public void onChanged(List<AttendanceRecord> attendanceRecords) {
                Log.i(TAG, "List of records: " + attendanceRecords);
                //Open success dialog

                for (AttendanceRecord aR : attendanceRecords) {
                    Log.i(TAG, "Location: " + aR.getLocation() +
                            " Check in: " + aR.getCheck_in() +
                            " Status " + aR.getStatus());
                    if (aR.getCheck_out() != null)
                        Log.i(TAG, "Check out: " + aR.getCheck_out());
                    if (aR.getLocation().equals(location) && aR.getStatus() == 1) {
                        setState(STATE_CHECK_OUT);
                        break;
                    } else {
                        setState(STATE_CHECK_IN);
                    }
                }
            }
        });

        fingerprintUtils = new FingerprintUtils(getActivity());

        FingerprintHandler fpHandler = new FingerprintHandler(getActivity());
        fpHandler.setListener(new FingerprintHandler.FingerprintAuthListener() {
            @Override
            public boolean onAuthFinished(boolean bool) {
                Log.i("Fingerprint", "received");

                if (bool){
                    //TODO: NETWORK ACTIVITY
                    createVerificationDialog(state);
                    mAttendanceViewModel.update(location);
                } else {
                    //Open failure dialog
                    createVerificationDialog(0);
                }

                if (dialog.isShowing()){
                    dialog.dismiss();
                }

                return bool;
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {

                  if (state == STATE_CHECK_OUT){
                      new AlertDialog.Builder(getActivity())
                              .setTitle("Confirm Check-Out")
                              .setMessage("Are you sure you wish to check out now?")
                              .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                  @Override
                                  public void onClick(DialogInterface alertDialog, int which) {
                                      dialog = new Dialog(getActivity());
                                      dialog.setContentView(R.layout.fp_dialog);
                                      dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                      dialog.show();
                                      fingerprintUtils.requestFingerprint(fpHandler);
                                  }
                              })
                              .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                  @Override
                                  public void onClick(DialogInterface dialog, int which) {
                                  }
                              })
                              .show();
                  }else {
                      dialog = new Dialog(getActivity());
                      dialog.setContentView(R.layout.fp_dialog);
                      dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                      dialog.show();
                      fingerprintUtils.requestFingerprint(fpHandler);
                  }
              }
          }
        );

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("update_bluetooth_fragment");

        if (getActivity() != null) {
            getActivity().registerReceiver(broadcastReceiver, intentFilter);

            //Update UI
//            Intent intent1 = new Intent();
//            intent1.setAction("update_bluetooth_fragment");
//            getActivity().sendBroadcast(intent1);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (getActivity() != null)
            getActivity().unregisterReceiver(broadcastReceiver);
    }

    @TargetApi(Build.VERSION_CODES.P)
    private void displayBiometricPrompt(final BiometricCallback biometricCallback) {
        new BiometricPrompt.Builder(getActivity())
                .setTitle("Fingerprint Authentication")
                .setSubtitle("Requesting Fingerprint")
                .setDescription("Touch your finger to the fingerprint sensor")
                .setNegativeButton("Cancel", getActivity().getMainExecutor(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getActivity(), "Fingerprint Authentication cancelled", Toast.LENGTH_SHORT).show();
                    }
                })
                .build();
    }


    private void setState(int state){
        this.state = state;
        switch (state){
            case STATE_NOT_AVAILABLE:
                button.setEnabled(false);
                bluetooth_image.setVisibility(View.VISIBLE);
                textView.setText(getString(R.string.scanning));
                button.setBackgroundColor(getResources().getColor(R.color.md_blue_600));
                button.setText(getString(R.string.check_in));;
                progressBar.setIndeterminate(true);
                progressBar.setColor(R.color.progress_bar_scanning);
                progressBar.setColor(ContextCompat.getColor(getActivity(), R.color.progress_bar_scanning));
                progressBar.setVisibility(View.VISIBLE);
                loadingDots.setVisibility(View.VISIBLE);
                break;
            case STATE_CHECK_IN:
                button.setEnabled(true);
                bluetooth_image.setVisibility(View.VISIBLE);
                textView.setText(getString(R.string.scanning));
                button.setBackgroundColor(getResources().getColor(R.color.md_blue_600));
                button.setText(getString(R.string.check_in));
                progressBar.setIndeterminate(true);
                progressBar.setColor(ContextCompat.getColor(getActivity(), R.color.progress_bar_scanning));
                progressBar.setVisibility(View.VISIBLE);
                loadingDots.setVisibility(View.VISIBLE);
                break;
            case STATE_CHECK_OUT:
                button.setEnabled(true);
                bluetooth_image.setVisibility(View.VISIBLE);
                textView.setText(getString(R.string.check_out_title));
                button.setBackgroundColor(getResources().getColor(R.color.md_blue_600));
                button.setText(getString(R.string.check_out));
                progressBar.setIndeterminate(false);
                progressBar.stopAnimation();
                progressBar.setProgress(100);
                progressBar.setColor(ContextCompat.getColor(getActivity(), R.color.progress_bar_check_out));
                progressBar.setVisibility(View.VISIBLE);
                loadingDots.setVisibility(View.INVISIBLE);

                break;
        }
    }


    /**
     States
     0 = Failed fp verification
     1 = Successful check-in
     2 = Successful check-out
     **/
    private void createVerificationDialog(int state){
        veri_dialog = new Dialog(getActivity());
        veri_dialog.setContentView(R.layout.verification_dialog);
        veri_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ImageView imageView = veri_dialog.findViewById(R.id.veri_dialog_image);
        TextView text = veri_dialog.findViewById(R.id.veri_dialog_text);
        TextView subtext = veri_dialog.findViewById(R.id.veri_dialog_subtext);
        MaterialButton button = veri_dialog.findViewById(R.id.veri_dialog_ok_button);;

        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                veri_dialog.dismiss();
            }
        });

        if (state == 0){
            imageView.setImageResource(R.drawable.ic_baseline_cancel_24);
            text.setText(getString(R.string.veri_dialog_failed));
            subtext.setText(getString(R.string.veri_dialog_failed_subtext));
            button.setBackgroundColor(getResources().getColor(R.color.cancel_circle_color));
        } else if (state == 2){
            text.setText(getString(R.string.veri_dialog_check_out));
            subtext.setText(getString(R.string.veri_dialog_check_out_subtext));
        }

        veri_dialog.show();
    }

}
