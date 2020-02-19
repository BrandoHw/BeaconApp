package org.altbeacon.Network;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

public abstract class DoWithProgress extends AsyncTask<Void, Void, Void> {

  private static final String TAG = "DoWithProgress";
  private WeakReference<Context> mContext;
  private Dialog dialog;

  public DoWithProgress(Context context) {
    mContext = new WeakReference<>(context);
  }


  @Override
  protected void onPreExecute() {
    super.onPreExecute();
    dialog = ProgressDialog.show(mContext.get(), "Processing...", "Please wait...");
  }

  @Override
  protected void onPostExecute(Void aVoid) {
    super.onPostExecute(aVoid);
    dialog.dismiss();
  }
}