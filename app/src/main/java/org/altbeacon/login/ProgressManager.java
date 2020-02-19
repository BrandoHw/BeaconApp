package org.altbeacon.login;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import org.altbeacon.WorkTracking.R;

/**
 * Created by atal on 13/05/2018.
 */

public class ProgressManager {
    private Activity activity;

    public ProgressManager(Activity activity) {
        this.activity = activity;
    }

    public void showProgress() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });
    }

    public void hideProgress() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.findViewById(R.id.progressBar).setVisibility(View.GONE);
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });
    }

    public void showAlert(final String title, final String msg) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Dialog dialog = new Dialog(activity);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.login_alert_dialog);

                TextView titleText = (TextView) dialog.findViewById(R.id.alert_title);
                titleText.setText(title);

                TextView msgText = (TextView) dialog.findViewById(R.id.alert_message);
                msgText.setText(msg);

                Button dialogButton = (Button) dialog.findViewById(R.id.alert_button);
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });
    }
}
