package org.altbeacon.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class GpsRequestService extends Service{

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
