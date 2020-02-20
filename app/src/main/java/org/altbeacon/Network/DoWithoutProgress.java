package org.altbeacon.Network;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.net.URI;

public abstract class DoWithoutProgress extends AsyncTask<URI, URI, URI> {

    private static final String TAG = "DoWithProgress";


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected void onPostExecute(URI uri) {
        super.onPostExecute(uri);

    }
}